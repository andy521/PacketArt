package jp.ac.kansai_u.kutc.firefly.packetArt.handlers;

/**
 * PcapManagerに搭載するためのインターフェースです。<br>
 * 搭載完了した場合、onNoPAcketsLeft関数は<br>
 * PcapManagerに、完全にパケットが無くなった時に0.1msくらいの間隔で呼ばれます。<br>
 *
 * @author sya-ke
*/
public interface OnNoPacketsLeftHandler{
    public void onNoPacketsLeft();
}
