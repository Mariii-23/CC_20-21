package control;

import module.menu.Menu;
import module.menu.MenuView;
import module.menu.Option;
import module.status.Information;

import java.util.ArrayList;

public class RunMenu implements Runnable {
  private final MenuView control;

  public RunMenu(Information information){
    this.control = new MenuView(information);
  }

  private static Menu<MenuView> menu() {
    var list = new ArrayList<Option<MenuView>>();
    list.add(new Option<>("Adicionar nome de ficheiro para ignorar" , MenuView::viewAddFileToIgnored));
    list.add(new Option<>("Terminar programa" , MenuView::viewTerminatedProgram, true));
    return new Menu<>("Menu Principal",list);
  }

  public void run() {
    var menu = menu();
    control.startMenu();
    menu.runMenu(control);
  }
}
