package jp.ac.kansai_u.kutc.firefly.packetArt.setting;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * 難易度に関するパネル
 * @author akasaka
 */
public class DifficultyPanel extends JPanel{
	final public String IMGPATH = new String("./resource/image/config/");
	private JRadioButton btnDifficultyStatic, btnDifficultyDynamic, btnDifficultyAuto;
	
	/**
	 * コンストラクタ
	 * @param 初期化前の難易度の設定
	 */
	DifficultyPanel(byte b){
		setLayout(new FlowLayout(FlowLayout.LEFT, 30, 0));
		JLabel labelDifficulty = new JLabel(new ImageIcon(IMGPATH + "labelDifficulty.png"));
		btnDifficultyStatic = new JRadioButton("静的");
	    btnDifficultyDynamic = new JRadioButton("動的");
	    btnDifficultyAuto = new JRadioButton("自動");
	    ButtonGroup difficultyGroup = new ButtonGroup();
	    difficultyGroup.add(btnDifficultyStatic); difficultyGroup.add(btnDifficultyDynamic);
	    difficultyGroup.add(btnDifficultyAuto);
		add(labelDifficulty);
		add(btnDifficultyStatic);
		add(btnDifficultyDynamic);
		add(btnDifficultyAuto);
		
		if     (b == ConfigStatus.STATIC)  btnDifficultyStatic.setSelected(true);
		else if(b == ConfigStatus.DYNAMIC) btnDifficultyDynamic.setSelected(true);
		else if(b == ConfigStatus.AUTO)    btnDifficultyAuto.setSelected(true);
	}
	
	/**
	 * 難易度の設定を取得する
	 * @return 難易度（静的, 動的, 自動）
	 */
	public byte getStatus(){
		if     (btnDifficultyStatic.isSelected())  return ConfigStatus.STATIC;
		else if(btnDifficultyDynamic.isSelected()) return ConfigStatus.DYNAMIC;
		else if(btnDifficultyAuto.isSelected())    return ConfigStatus.AUTO;
		return -1;
	}
}
