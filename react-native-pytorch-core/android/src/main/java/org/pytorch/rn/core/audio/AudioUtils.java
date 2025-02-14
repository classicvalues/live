/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.pytorch.rn.core.audio;

import android.media.MediaDataSource;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AudioUtils {

  public static final String TAG = "PTLAudioUtils";

  private static final int REQUEST_RECORD_AUDIO = 13;
  private static final int SAMPLE_RATE = 16000;
  private static final int CHANNELS = 1;
  private static final int PCM_BITS = 16;
  private static final int HEADER_FORMAT_PCM = 1;
  private static final int VOLUME_CALLBACK_RATE_PER_SEC = 10;
  private static final int SAMPLES_PER_VOLUME_CALLBACK = SAMPLE_RATE / VOLUME_CALLBACK_RATE_PER_SEC;
  private static final int BYTES_PER_SAMPLE = PCM_BITS / 8;
  private static final int HEADER_SIZE_BYTES = 44;
  private static final int BUFFER_SIZE = 4096;

  private static final String DEFAULT_EXTENSION = ".wav";
  private static final String DEFAULT_NAME = "audio";

  public static MediaDataSource getAudioAsMediaDataSource(short[] data) {
    try {
      byte[] audioDataBytes = AudioUtils.toByteArray(data);
      byte[] header =
          getWaveHeader(
              (int) audioDataBytes.length, HEADER_FORMAT_PCM, CHANNELS, SAMPLE_RATE, PCM_BITS);
      byte[] completeAudioFileData = Arrays.copyOf(header, header.length + audioDataBytes.length);
      System.arraycopy(
          audioDataBytes, 0, completeAudioFileData, header.length, audioDataBytes.length);
      return new AudioDataSource(completeAudioFileData);
    } catch (Exception e) {
      Log.e(TAG, "Exception while creating the audio data source : ", e);
      return null;
    }
  }

  /**
   * Helper method to convert an array of short to an array of byte.
   *
   * @param data short[] to be converted
   * @return converted byte[]
   */
  private static byte[] toByteArray(final short[] data) {
    int index;
    int iterations = data.length;
    ByteBuffer bb = ByteBuffer.allocate(data.length * 2);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    for (index = 0; index != iterations; ++index) {
      bb.putShort(data[index]);
    }
    return bb.array();
  }

  /**
   * Helper method to generate a standard 'wav' file header.
   *
   * @param fileSize Size of the audio data
   * @param audioFormat Audio Header format
   * @param channels Number of channels
   * @param sampleRate Sample rate of the audio
   * @param bitsPerSample Bits per sample of the audio
   * @return byte[] data of the header of the wav file
   */
  private static byte[] getWaveHeader(
      int fileSize, int audioFormat, int channels, int sampleRate, int bitsPerSample) {
    int byteRate = sampleRate * bitsPerSample * channels / 8;
    int bitSampleChannelRate = bitsPerSample * channels / 8;
    int chunkSize = fileSize - 8; // The file size - 8 bytes for RIFF and chunk size.
    int audioLength = fileSize - HEADER_SIZE_BYTES; // The file size - 44 bytes.

    byte[] header = new byte[HEADER_SIZE_BYTES];
    header[0] = 'R'; // RIFF in 4 bytes.
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (byte) (chunkSize & 0xff); // Chunk size in 4 bytes.
    header[5] = (byte) ((chunkSize >> 8) & 0xff);
    header[6] = (byte) ((chunkSize >> 16) & 0xff);
    header[7] = (byte) ((chunkSize >> 24) & 0xff);
    header[8] = 'W'; // WAVE in 4 bytes.
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    header[12] = 'f'; // 'fmt ' chunk in 4 bytes with trailing space in 4 bytes.
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';
    header[16] = 16; // 4 bytes: size of 'fmt ' chunk in 4 bytes.
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    header[20] = (byte) audioFormat; // Format (1 for PCM) in 2 bytes.
    header[21] = 0;
    header[22] = (byte) channels; // Number of channels in 2 bytes.
    header[23] = 0;
    header[24] = (byte) (sampleRate & 0xff); // Sample rate in 4 bytes.
    header[25] = (byte) ((sampleRate >> 8) & 0xff);
    header[26] = (byte) ((sampleRate >> 16) & 0xff);
    header[27] = (byte) ((sampleRate >> 24) & 0xff);
    header[28] = (byte) (byteRate & 0xff); // Byte rate in 4 bytes.
    header[29] = (byte) ((byteRate >> 8) & 0xff);
    header[30] = (byte) ((byteRate >> 16) & 0xff);
    header[31] = (byte) ((byteRate >> 24) & 0xff);
    header[32] = (byte) bitSampleChannelRate; // bitSampleChannelRate in 2 bytes.
    header[33] = 0;
    header[34] = (byte) bitsPerSample; // Bits per sample in 2 bytes.
    header[35] = 0;
    header[36] = 'd'; // Data in 4 bytes.
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (byte) (audioLength & 0xff);
    header[41] = (byte) ((audioLength >> 8) & 0xff);
    header[42] = (byte) ((audioLength >> 16) & 0xff);
    header[43] = (byte) ((audioLength >> 24) & 0xff);
    return header;
  }
}
