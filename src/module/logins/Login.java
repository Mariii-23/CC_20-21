package module.logins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class Login {
  private final HashMap<String, String> combination;
  private final String fileName;

  public Login(String fileName) {
    this.fileName = fileName;
    this.combination = new HashMap<>();
  }

  public void readAutenticationFile() {
    try {
      File file = new File(fileName);
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        var temparr = line.split(";", 2);
        combination.put(temparr[0], temparr[1]);
      }
      fr.close();    //closes the stream and release the resources
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean validate(String givenKey, String recievedValue) {
    return combination.get(givenKey).equals(recievedValue);
  }

  public String autenticate(String key) {
    return combination.get(key);
  }

  private int randomNumber() {
    Random r = new Random();
    return r.nextInt(combination.size());
  }

  public String generateKey() {
    int random = randomNumber();
    int i = 0;
    for (var elem : combination.keySet())
      if (i == random) return elem;
      else i++;
    return null;
  }
}
