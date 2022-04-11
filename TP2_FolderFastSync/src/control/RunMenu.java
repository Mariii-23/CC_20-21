package control;

import module.log.Log;
import module.menu.Menu;
import module.menu.MenuView;
import module.menu.Option;
import module.status.Information;
import module.status.SeqPedido;

import java.net.InetAddress;
import java.util.ArrayList;

public class RunMenu implements Runnable {
  private final MenuView control;

  public RunMenu(Information information, int port, InetAddress clientIp, Log log, SeqPedido seqPedido){
    this.control = new MenuView(information, port, clientIp, log, seqPedido);
  }

  private static Menu<MenuView> menu() {
    var list = new ArrayList<Option<MenuView>>();
    list.add(new Option<>("Adicionar nome de ficheiro para ignorar" , MenuView::viewAddFileToIgnored));
    list.add(new Option<>("Adicionar nome de ficheiro para sincronizar" , MenuView::viewAddFileToSynchronize));
    list.add(new Option<>("Ver ficheiros que não estão a ser sincronizados", MenuView::viewShowFilesToIgnored ));
    list.add(new Option<>("Terminar programa" , MenuView::viewTerminatedProgram, true));
    return new Menu<>("Menu Principal",list);
  }

  public void run() {
    var menu = menu();
    control.startMenu();
    menu.runMenu(control);
  }
}
