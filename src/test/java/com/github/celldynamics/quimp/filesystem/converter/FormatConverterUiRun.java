package com.github.celldynamics.quimp.filesystem.converter;

/**
 * @author p.baniukiewicz
 *
 */
public class FormatConverterUiRun {
  static {
    // disable to redirect all above INFO to internal console
    // System.setProperty("logback.configurationFile", "quimp-logback.xml");
    System.setProperty("quimpconfig.superDebug", "false");
  }

  /**
   * Runner.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    // try {
    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (ClassNotFoundException e) {
    // e.printStackTrace();
    // } catch (InstantiationException e) {
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // e.printStackTrace();
    // } catch (UnsupportedLookAndFeelException e) {
    // e.printStackTrace();
    // }
    FormatConverterController obj = new FormatConverterController();
    obj.showUi();
  }

}
