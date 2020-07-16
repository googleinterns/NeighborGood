// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.neighborgood.servlets;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that turns speech into text. */
@WebServlet("/speech")
public class RecognizeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
    SpeechClient client = SpeechClient.create();
    ResponseObserver<StreamingRecognizeResponse> responseObserver =
        new ResponseObserver<StreamingRecognizeResponse>() {
          List<StreamingRecognizeResponse> responses = new ArrayList<>();

          // Implement the onStart() function called before the stream is started
          public void onStart(StreamController controller) {
            // Print a message to indicate that the stream is ready to start
            System.out.println("Speech recognize stream is ready to start.");
          }

          // Implement the onResponse() function called when receving a response from the stream
          public void onResponse(StreamingRecognizeResponse response) {
            // Add the response to the list
            responses.add(response);
          }

          // Implement the onComplete() function called when receiving a notification of successful
          // stream completion
          public void onComplete() {
            String finalResult = "";
            for (StreamingRecognizeResponse response : responses) {
              StreamingRecognitionResult result = response.getResultsList().get(0);
              SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
              finalResult += alternative.getTranscript();
            }
          }
        };
  }
}
