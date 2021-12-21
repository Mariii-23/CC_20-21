package module.menu;

import module.status.Information;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MenuView {
  private final Information information;

  public MenuView(Information information){
    this.information = information;
  }

  private static String readString() {
    var s = new Scanner(System.in);
    return s.nextLine();
  }

  private static int readInt() throws InputMismatchException {
    var s = new Scanner(System.in);
    return s.nextInt();
  }

  public void viewAddFileToIgnored() {
    try {
      System.out.println("File name: ");
      var name = readString();
      information.addFileToIgnored(name);
    } catch (Exception e) {
      System.out.println("Nome do ficheiro dado errado");
    }
  }

  public void viewTerminatedProgram(){
    //TODO mandar o bye primeiro
    information.endProgram();
  }

  public void startMenu() {
    information.startMenu();
  }
}
