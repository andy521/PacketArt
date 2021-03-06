package jp.ac.kansai_u.kutc.firefly.packetArt;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

/**
 * 描画を行うためのフォームの基底となる抽象クラスです。<br>
 * FormBaseを継承する場合は以下のメソッドを実装してください。<br>
 * initialize, paint, upadateメソッド <br>
 * MouseListener, KeyListener, MouseMotionListenerインタフェースの実装(任意)
 * @author midolin
 */
public abstract class FormBase extends Canvas implements MouseListener, KeyListener, MouseMotionListener {
	Graphics offgc;
	Image offscreen = null;
	boolean enableAntiAlias;
	JFrame parentForm;

	/**
	 * フォームオブジェクトを生成します。このメソッド内で例外が発生しないように善処してください。
	 */
	protected FormBase(){
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		enableAntiAlias = true;
		setVisible(false);
		setBackground(Color.black);
	}

	/**
	 * クラスの初期化を行います。このメソッドはフォーム初期化時に1回だけ呼び出されます。
	 *
	 */
	public abstract void initialize();

	/**
	 * 描画処理を行います。
	 * この処理は16ミリ秒に1回呼び出されることに注意してください。
	 * また、突然呼び出されなくなることに注意してください。
	 */
	public abstract void paint(Graphics g);

	/**
	 * 更新処理を行います。
	 * この処理は16ミリ秒に1回呼び出されることに注意してください。
	 * また、突然呼び出されなくなる可能性もあるので注意してください。
	 */
	public abstract void update();

	/**
	 * フォームが閉じられたときに呼び出されます。
	 */
	public abstract void onClose();
	/**
	 * フォームが変更されるときに呼び出されます。
	 */
	public abstract void onFormChanged();

	public void paintComponent(Graphics g){
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			getAntiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	// http://www.ecst.csuchico.edu/~amk/classes/csciOOP/double-buffering.html
	public void update(Graphics g) {
		Dimension d = getSize();

		// create the offscreen buffer and associated Graphics
		offscreen = createImage(d.width, d.height);
		offgc = offscreen.getGraphics();
		// clear the exposed area
		offgc.setColor(getBackground());
		offgc.fillRect(0, 0, d.width, d.height);
		offgc.setColor(getForeground());
		// do normal redraw
		paint(offgc);
		// transfer offscreen to window
		g.drawImage(offscreen, 0, 0, this);
	}

	/**
	 * アンチエイリアスの有効/無効を指定します。
	 * @param value アンチエイリアスの有効/無効
	 */
	public void setAntiAlias(boolean value){
		enableAntiAlias = value;
	}
	/**
	 * 現在のアンチエイリアスの有効/無効を取得します。
	 * @return アンチエイリアスの有効/無効
	 */
	public boolean getAntiAlias(){
		return enableAntiAlias;
	}

	/**
	 * このフォームを含む親フレームを取得します。
	 * @return 親フレーム
	 */
	public JFrame getParentFrame(){
		return parentForm;
	}
	/**
	 * このフォームを含む親フレームを指定します。
	 * @param 親フレーム
	 */
	public void setParentFrame(JFrame frame){
		parentForm = frame;
	}

	/**
	 * このフォームに何らかのComponentを配置するときに使用します。
	 */
	protected Container getContentPane(){
		return getParent();
	}
}