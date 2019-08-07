package pt.iknow.floweditor;

/*****************************************************
 *
 *  Project FLOW EDITOR
 *
 *  class: Sair
 *
 *  desc: classe que pergunta se quer iSAIR/gravar
 *
 ****************************************************/

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/*******************************
 * Sair
 */
public class Sair extends JDialog {
  private static final long serialVersionUID = 1L;

  public static final int YES = JOptionPane.YES_OPTION;
  public static final int NO = JOptionPane.NO_OPTION;
  public static final int CANCEL = JOptionPane.CANCEL_OPTION;

  protected int iSAIR = JOptionPane.CANCEL_OPTION;

  /****************************************************
   * cria uma caixa de dialogo  a perguntar sim ou nao
   */
  public Sair(JFrame janela, String option) {
    Object[] options = { Mesg.Sim, Mesg.Nao, Mesg.Cancelar };
    int n = NO;

    /* gravar */
    if (option.equals(Mesg.MenuGravar)) {
      n = JOptionPane.showOptionDialog(janela, Mesg.Pergunta2, Mesg.Nome_Janela2, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE, (Icon) null, options, options[1]);
    } else if (option.equals(Mesg.MenuSair)) {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(new JLabel(Mesg.Pergunta), BorderLayout.CENTER);
      JCheckBox checkBox = new JCheckBox(Mesg.Repetir);
      panel.add(checkBox, BorderLayout.SOUTH);
      FlowEditorConfig cfg = FlowEditorConfig.loadConfig();
      if(cfg.isConfirmExit()) {
        n = JOptionPane.showOptionDialog(janela, panel, Mesg.Nome_Janela, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, (Icon) null, options, options[1]);
        if(checkBox.isSelected()) {
          cfg.setConfirmExit(false);
          cfg.saveConfig();
        }
      } else {
        n = YES;
      }

    } else if (option.equals(Mesg.ConfirmaAlterados)) {
      options = new Object[]{ Mesg.Sim, Mesg.Nao };
      n = JOptionPane.showOptionDialog(janela, Mesg.Pergunta3, Mesg.Nome_Janela3, JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE, (Icon) null, options, options[1]);
    } else if (option.equals(Mesg.ConfirmaExistente)) {
      n = JOptionPane.showOptionDialog(janela, Mesg.Pergunta4, Mesg.Nome_Janela4, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE, (Icon) null, options, options[1]);
    }
    iSAIR = n;

  }

  /***************************************************************************
   * saber resultado da pergunta
   * @deprecated usar getResposta
   */
  public int Resposta() {
    return getResposta();
  }

  /***************************************************************************
   * saber resultado da pergunta
   */
  public int getResposta() {
    return iSAIR;
  }

}


