package scripts;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class PrintPuzzles {

  public static void main(String[] args) throws Exception {
    String[][] samplePieceArray = new String[8][8];
    samplePieceArray[2][2] = "k";
    samplePieceArray[4][2] = "K";
    samplePieceArray[6][2] = "P";
    String fen = pieceArrayToFen(samplePieceArray);
    System.out.println(fen);

    Optional<JSONObject> tablebaseResponse = fetchResponseFromTablebase(fen);
    if (!tablebaseResponse.isPresent()) {
      System.out.println("Failed to fetch response.");
      return;
    }

    System.out.println(tablebaseResponse.get());
  }

  private static String pieceArrayToFen(String[][] pieceArray) {
    String fen = "";
    for (int row = 0; row < 8; row++) {
      int consecutiveBlanks = 0;
      for (int col = 0; col < 8; col++) {
        if (pieceArray[row][col] == null) {
          consecutiveBlanks++;
          continue;
        }

        if (consecutiveBlanks > 0) {
          fen += consecutiveBlanks;
          consecutiveBlanks = 0;
        }
        fen += pieceArray[row][col];
      }
      if (consecutiveBlanks > 0) {
        fen += consecutiveBlanks;
      }
      if (row < 7) {
        fen += "/";
      }
    }
    return fen;
  }

  private static Optional<JSONObject> fetchResponseFromTablebase(String fen) throws Exception {
    URL url = new URL("http://tablebase.lichess.ovh/standard?fen=" + fen);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Content-Type", "application/json");

    if (connection.getResponseCode() != 200) {
      return Optional.empty();
    }

    StringBuffer response = new StringBuffer();
    String inputLine;
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    while ((inputLine = reader.readLine()) != null) {
      response.append(inputLine);
    }
    reader.close();

    return Optional.of(new JSONObject(response.toString()));
  }
}