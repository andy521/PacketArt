package jp.ac.kansai_u.kutc.firefly.packetArt.setting;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jp.ac.kansai_u.kutc.firefly.packetArt.PlaySE;

/**
 * ログの表示設定に関するパネル
 * @author akasaka
 */
public class ViewLogPanel extends JPanel{
	private JRadioButton btnOn, btnOff;
	
	/**
	 * コンストラクタ
	 * @param 初期化前のログ表示設定
	 */
	ViewLogPanel(boolean f){
		setLayout(new FlowLayout(FlowLayout.LEFT, ConfigInfo.HGAP, 0));
		setOpaque(false);
		
		JLabel labelViewLog = new JLabel(new ImageIcon(PlaySE.getBytes(this.getClass().getResourceAsStream(ConfigInfo.IMGPATH + "labelLog.png"))));
		btnOn  = new JRadioButton(new ImageIcon(PlaySE.getBytes(this.getClass().getResourceAsStream(ConfigInfo.BTNPATH + "btnOn.png"))));
		btnOff = new JRadioButton(new ImageIcon(PlaySE.getBytes(this.getClass().getResourceAsStream(ConfigInfo.BTNPATH + "btnOff.png"))));

		ButtonGroup logViewGroup = new ButtonGroup();
		logViewGroup.add(btnOn); logViewGroup.add(btnOff);
		
		btnOn .setSelectedIcon(new ImageIcon(PlaySE.getBytes(this.getClass().getResourceAsStream(ConfigInfo.BTNPATH + "btnOnSelected.png"))));
		btnOff.setSelectedIcon(new ImageIcon(PlaySE.getBytes(this.getClass().getResourceAsStream(ConfigInfo.BTNPATH + "btnOffSelected.png"))));
		
		btnOn .setContentAreaFilled(false);
		btnOff.setContentAreaFilled(false);
		
		add(labelViewLog);
		add(btnOn);
		add(btnOff);
		
		if(f) btnOn .setSelected(true);
		else  btnOff.setSelected(true);
	}
	
	/**
	 * ログの表示設定を取得する
	 * @return ログ表示のオン・オフ
	 */
	public boolean getStatus(){ return btnOn.isSelected(); }
}
