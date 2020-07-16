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

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/** Servlet that turns speech into text. */
@WebServlet("/speech")
public class RecognizeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            try {
              response.setContentType("text/html;");
              response.getWriter().println(finalResult);
            } catch (IOException e) {
              System.out.println("When printing response, exception " + e + " occurs.");
            }
          }

          // Implement the onError() function called when receiving a terminating error
          public void onError(Throwable t) {
            System.err.println(t);
          }
        };

    ClientStream<StreamingRecognizeRequest> clientStream =
        client.streamingRecognizeCallable().splitCall(responseObserver);

    // Define a recognition configuration with language set to English and sample rate set to
    // 16000Hz
    RecognitionConfig recognitionConfig =
        RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setLanguageCode("en-US")
            .setSampleRateHertz(16000)
            .build();

    StreamingRecognitionConfig streamingRecognitionConfig =
        StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

    // The config will be the first reques
    StreamingRecognizeRequest streamingRecognizeRequest =
        StreamingRecognizeRequest.newBuilder()
            .setStreamingConfig(streamingRecognitionConfig)
            .build();

    clientStream.send(streamingRecognizeRequest);

    // Set the sample rate hertz of the audio format to 16000, sample size to 16bits and only one
    // channel
    AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);

    // Receive input from the device microphone stream
    DataLine.Info targetInfo = new Info(TargetDataLine.class, audioFormat);

    if (!AudioSystem.isLineSupported(targetInfo)) {
      // If microphone not supported, send an error message to the user
      System.err.println("Microphone not supported");
      response.setContentType("text/html;");
      response.getWriter().println("Microphone is not supported on the device");
      System.exit(0);
    }

    try {
      TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
      targetDataLine.open(audioFormat);
      targetDataLine.start();
      long startTime = System.currentTimeMillis();

      // Start receiving input audio stream
      AudioInputStream audio = new AudioInputStream(targetDataLine);
      while (true) {
        long estimatedTime = System.currentTimeMillis() - startTime;
        byte[] data = new byte[6400];
        audio.read(data);
        // After 30 seconds, stop receiving input
        if (estimatedTime > 30000) {
          targetDataLine.stop();
          targetDataLine.close();
          break;
        }
        streamingRecognizeRequest =
            StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(data))
                .build();
        clientStream.send(streamingRecognizeRequest);
      }
    } catch (LineUnavailableException e) {
      System.out.println("An exception " + e + " is thrown.");
    }

    responseObserver.onComplete();
  }
}
