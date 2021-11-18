package module.logins;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class Login {
  HashMap<String,String> combination;
  String fileName;

  public Login(String fileName) {
    this.fileName = fileName;
    this.combination = new HashMap<>();
  }

  public void readAutenticationFile(){
    String[] temparr = new String[2];

    Scanner myReader = new Scanner(fileName);
    while (myReader.hasNextLine()) {
      String data = myReader.nextLine();
      temparr = data.split(";", 2);
      combination.put(temparr[0], temparr[1]);
    }
    myReader.close();
  }

  public boolean validate(String givenKey, String recievedValue) {
    return combination.get(givenKey).equals(recievedValue);
  }

  public String autenticate(String key) {
    return combination.get(key);
  }

  public String generateKey() {
    Random r = new Random();
    return combination.get(combination.get(r.nextInt(combination.size())));
  }


}
