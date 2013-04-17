package jp.ac.kansai_u.kutc.firefly.packetArt.readTcpDump;

import java.io.File;//File.existsに必要
import java.net.InetAddress;//getDevByIPのIPからStringの変換に必要
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;//こいつが心臓
import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.PcapSockAddr;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.PcapDLT;//イーサネット２。
import org.jnetpcap.PcapBpfProgram;

/*

    TODO: パケット３０００個の保持{
        上記デバイスからの読み込みに際し、jnetpcapにより
        長蛇の待ちパケット行列がメモリ上に生成される可能性が発生した。
        これは普通のtcpdumpにも言える問題であるが、運営から言われたなら
        しかたない。パケットの間引きにBPFを使うのは
        パケットの種類の選択肢が減ることになる。。。
        そのために、パケットをある程度確保したら捨てる方式をとる。
        これなら、使えるパケットの種類を意図的に減らすことはなくなる。
        3000個の確保にはjava.util.Queueを使う。捨てるには、nextPacketを空撃ち。
        nextPacketは透過的に提供する。キューには定期的に補充し、
        キューと、jnetpcapのキューに無くなり次第、関数はnullを返すようにする。
        また、fromFile == Trueのときは空撃ちしない。
    }

    TODO: 透過的にBPFフィルタを追加する関数を作成する。
          scapyのように途中でフィルタを変更するというのはできないっぽい。

    TODO: 残りパケットの（概算値）の保持{
        ファイルサイズ(Byte)を標準MTU,1500byteで割る。WIDEプロジェクトの
        パケットはMTU=1500が最頻値なので、逐次ロードでもおおよそは出せるはずだ
    }

*/

/**
 * 完全にパケットアート用。
 * 使い方:
 * PcapManager pm = new PcapManager(new File(filename))
 * if (pm.isReadyRun) { pm.nextPacket(); }
 * 
 * その他アクセスできる変数はget??というメソッドを参照してください。
 * 正常にパケットを読めたか確認するにはisReadyRunを使います。
*/
public class PcapManager {
    static final PcapManager instance = new PcapManager();
    public static PcapManager getInstance(){
       return instance;
    }


    private static StringBuilder errBuf;//libpcapからのエラーをここに
    private File pcapFile;
    private PcapIf pcapDev;
    private boolean fromFile;
    private boolean fromDev;
    private boolean readyRun;
    private Pcap pcap;//jnetpcapの核。
    private PcapBpfProgram bpfFilter;
    //private Queue packetQueue;

    /**
     * @return pcapFile 現在開いているtcpdumpのファイルオブジェクトを返します。
    */
    public File getPcapFile() {
        return pcapFile;
    }

    /**
     * @return pcapDev 現在開いているデバイス(PcapIF)オブジェクトを返します。
    */
    public PcapIf getPcapDev() {
        return pcapDev;
    }

    /**
     * @return fromFile 現在ファイルからパケットを読み込んでいるか、否か。
    */
    public boolean isFromFile() {
        return fromFile;
    } 

    /**
     * @return fromDev 現在デバイスからパケットを読み込んでいるか、否か。
    */
    public boolean isfromDev() {
        return fromDev;
    } 

    /**
     * @return readyRun 現在ファイルもしくはデバイスをオープンしているか、否か。
    */
    public boolean isReadyRun() {
        return readyRun;
    } 

    /**
     * @return errBuf.toString() 現在保持しているエラー情報を返します。
    */
    public String getAllErr() {
        return errBuf.toString();
    }

    /**
     * @return pcap.getErr() libpcapに関する最新のエラー情報を返します。
    */
    public String getErr() {
        return pcap.getErr();
    }

    /**
     * 空のコンストラクタ。このコンストラクタを使う場合は、オブジェクト生成後に
     * openDev(name)もしくはopenFile(name)をしないとパケットが読めません。。
    */
    private PcapManager() {
        init();
        System.out.println("PcapManager()");
    }

    /**
     * ローカルのファイルからパケットを読み出す。
     * @param file tcpdumpファイルのFileオブジェクト。読み込みできるようにね。
    */
    private PcapManager(File file) {
       init();
        System.out.println("PcapManager(File " + file.getName() +")");
        openFile(file.getName());
    }

    /**
     * ローカルのデバイスからパケットを読み出す。
     * @param dev リッスンしたいデバイスのPcapIfオブジェクト。
    */
    private PcapManager(PcapIf dev) {
        init();
        System.out.println("PcapManager(PcapIf " + dev.getName() +")");
        openDev(dev.getName());
    }

    /**
     * Linuxならeth0だが、WindowsでデバイスIDを取得するのはタイヘン。
     * そこで、様々な文字列からお目当てのパケットを取得できるようにする。
     * 使うなら絶対このコンストラクタ！
     *
     * @param name ファイル名フルパスもしくはデバイスのIPを、Stringで。
    */
    private PcapManager(String name) {
        init();
        System.out.println("PcapManager(String " + name +") -> ***GUESS***");
        if (name == null) {
            return;
        }
        //name はFilePathか？
        pcapFile = new File(name);
        if (pcapFile.exists() ) {
            openFile(name);
            return;
        }

        //nameはデバイスIDか？
        openDev(name);
        //nameは・・・・何コレ？
        System.err.println("PcapManager failed guess what the " + name + " is.");
    }

    /**
     * どのコンストラクタでも最初に呼ばれます。ただの初期化関数です。
     * 何のエラーも引数も返り値もありません。
    */
    public void init() {
        System.out.println("PcapManager.init()");
        File pcapFile = null;
        PcapIf pcapDev = null;
        pcap = null;
        fromFile = false;
        fromDev = false;
        readyRun = false;
        errBuf = new StringBuilder();
    }

    /**
     * Fileがコンストラクタの引数の場合に呼ばれる関数です。
     * この関数では例外は発生しません。
     *
     * @return wasOK 成功か失敗か。失敗ならerrBufにエラーが入ってます。
    */
    public boolean openFile(String fname) {
        System.out.println("openFile(" + fname +")");
        boolean wasOK = false;
        pcap = Pcap.openOffline(fname,errBuf);
        if (pcap == null) {
            System.err.println("Error while opening a file for capture: "
                + errBuf.toString());
            return wasOK;
        }
        pcapFile = new File(fname);
        fromFile = true;
        wasOK = true;
        readyRun = wasOK;
        return wasOK;
    }

    /**
     * @return wasOK 成功か失敗か。
    */
    public boolean openDev() {
        String devName = Pcap.lookupDev(errBuf);
        System.out.println("openDev(" + devName +  ")");
        return openDev(devName);
    }

    /**
     * デバイス名がコンストラクタの引数の場合に呼ばれる。
     * openLiveでは例外は発生しない。
     *
     * @return wasOK 成功か失敗か。
    */
    public boolean openDev(String devName) {
        System.out.println("openDev(" + devName +")");
        boolean wasOK = false;
        pcap = Pcap.openLive(devName, Pcap.DEFAULT_SNAPLEN, Pcap.MODE_PROMISCUOUS, Pcap.DEFAULT_TIMEOUT, errBuf);
        if (pcap == null) {
            System.err.println("Error while opening device for capture: "
                + errBuf.toString());
            return wasOK;
        }
        fromDev = true;
        wasOK = true;
        readyRun = wasOK;
        return wasOK;
    }

    /**
     * 一個ずつロードします。packetのメモリはlibpcapのメモリを共有しています。
     * こいつに関する参照を無くすとlibpcapのメモリもFreeされます。多分。
     * メモリのアロケート処理が入らないので、高速？
     * @return packet パケット。というかlibpcapの保持するパケットへのポインタ。
    */
    public PcapPacket nextPacket() {
        PcapPacket packet = new PcapPacket(JMemory.POINTER);
        if ( pcap.nextEx(packet) == Pcap.NEXT_EX_OK ) {
          return packet;
        } else {
            readyRun = false;
            return null;
        }
    }

    /**
     * 一個ずつロードします。packetはのメモリはJavaで管理されます。
     * libpcapの保持するパケットはすぐに解放され、その代わりにJavaのメモリを食います
     * メモリのアロケート処理が入るので、低速？
     * @return pkt パケット。libpcapの方はすぐに解放される。メモリ的に安全。
    */
    public PcapPacket nextPacketCopied() {
        PcapPacket pkt = nextPacket();
        if (pkt != null) {
            return new PcapPacket(pkt);
        }
        return null;
    }
    /*TODO:
    public float nokoriPacket() {
        int MTU = 1500;
        int FILESIZE = File.getSize();
        int howManyPackets = FILESIZE/MTU;
        float ret = count / howManyPackets;
    }
    */

    /**
     * BPFという記法で取得するパケットを意図的に制御します。
     * @param bpf BPF構文で書かれたフィルタリングの記号文字列
     * @return T/F 成功か、失敗か。エラーは発生しませんが、getErr()でエラー内容は見れます。
    */
    public boolean setBPFfilter(String bpf) {
        if (bpfFilter != null) {
            System.out.println("ERRO: Has been Fltered!");
            return false;
        }
        final int OPTIMIZE = 1;//立てといた方がいいんでしょ？多分。
        final int NETMASK = 0;//今回はWANのお話なので。。。
        final int DLT = PcapDLT.CONST_EN10MB;//イーサネット２。
        PcapBpfProgram filter = new PcapBpfProgram();//BPFポインタを取得

        int retCode;
        if (readyRun && pcap != null) { //オープン済み
            retCode = pcap.compile(filter, bpf, OPTIMIZE, NETMASK);
        } else { //未オープン。snaplenとDLTの指定が必要。
            retCode = Pcap.compileNoPcap(Pcap.DEFAULT_SNAPLEN,
                                DLT,
                                filter,
                                bpf,
                                OPTIMIZE,
                                NETMASK);
        }
        if ( retCode == Pcap.NOT_OK ) { // 多分、String bpfがBPFの構文にあってない
            System.out.println("PcapManager.getBPF('"+ bpf +"') Failed!");
            if (pcap != null) {
                System.out.println("ERROR is " + pcap.getErr());
            }
            return false;
        }
        if ( pcap != null && pcap.setFilter(filter) == Pcap.NOT_OK){
            System.out.println("PcapManager.getBPF('"+ bpf +"') Failed!");
            if (pcap != null) {
                System.out.println("ERROR is " + pcap.getErr());
            }
            return false;
        }
        bpfFilter = filter;//解放用ポインタの保持。
        return true;
    }

    /**
     * アロケートしたメモリや開いたファイルをガベコレの前に閉じます。
    */
    public void close() {
        if ( fromFile ) {
            pcap.close();
        }
        if ( bpfFilter != null ) {
            Pcap.freecode(bpfFilter);
        }
    }
}
