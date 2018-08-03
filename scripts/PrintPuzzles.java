package scripts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class PrintPuzzles {

  public static void main(String[] args) throws Exception {
    while (true) {
      String[][] pieceArray = generateRandomPieceArray();
      String fen = pieceArrayToFen(pieceArray);
      Optional<JSONObject> tablebaseResponse = fetchResponseFromTablebase(fen);
      if (!tablebaseResponse.isPresent()) {
        continue;
      }
      if (!isInterestingResponse(tablebaseResponse.get())) {
        continue;
      }
      System.out.println(fen);
    }
  }

  private static String[][] generateRandomPieceArray() {
    String[][] samplePieceArray = new String[8][8];
    samplePieceArray[randomCoordinate()][randomCoordinate()] = "k";
    samplePieceArray[randomCoordinate()][randomCoordinate()] = "K";
    samplePieceArray[randomCoordinate()][randomCoordinate()] = "R";
    samplePieceArray[randomCoordinate()][randomCoordinate()] = "n";
    return samplePieceArray;
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
    URL url = new URL("http://localhost:9000/standard?fen=" + fen);
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

  private static boolean isInterestingResponse(JSONObject response) throws Exception {
    if (response.getInt("wdl") != 2) {
      // The position is not winning for white.
      return false;
    }
    JSONArray moves = response.getJSONArray("moves");
    if (moves.length() < 2) {
      // There is only one legal move.
      return false;
    }
    int numWinningMoves = 0;
    String winningMoveSan = "";
    for (int i = 0; i < moves.length(); i++) {
      JSONObject move = moves.getJSONObject(i);
      if (move.getInt("wdl") != -2) {
        // This move is not winning for white.
        continue;
      }
      numWinningMoves++;
      winningMoveSan = move.getString("san");
    }

    if (numWinningMoves > 1) {
      // There is more than one winning move for white.
      return false;
    }
    // The position is interesting if the only winning move is not a capture.
    return !winningMoveSan.contains("x");
  }

  private static int randomCoordinate() {
    return (int) (8 * Math.random());
  }
}