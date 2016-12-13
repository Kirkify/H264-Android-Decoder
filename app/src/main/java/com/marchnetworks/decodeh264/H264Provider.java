package com.marchnetworks.decodeh264;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class H264Provider {

    private byte[] sps_params = null;
    private byte[] pps_params = null;
    private byte[] i_frame = null;

    public H264Provider(InputStream inStream) {

        try {
            int firstStartCodeIndex = 6;
            int secondStartCodeIndex = 0;
            int thirdStartCodeIndex = 0;

            //byte[] data = new byte[inStream.available()];
            byte[] data = toByteArray(inStream);

            for (int i = 0; i < 100; i++)
            {
                if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01)
                {
                    if ((data[i + 4] & 0x1F) == 7) //SPS
                    {
                        firstStartCodeIndex = i;
                        break;
                    }
                }
            }

            int firstNaluSize = 0;

            for (int i = firstStartCodeIndex + 4; i < firstStartCodeIndex + 100; i++)
            {
                if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01)
                {
                    if (firstNaluSize == 0)
                    {
                        firstNaluSize = i - firstStartCodeIndex;
                    }
                    if ((data[i + 4] & 0x1F) == 8) //PPS
                    {
                        secondStartCodeIndex = i;
                        break;
                    }
                }
            }

            int secondNaluSize = 0;

            for (int i = secondStartCodeIndex + 4; i < secondStartCodeIndex + 130; i++)
            {

                if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01)
                {
                    if (secondNaluSize == 0)
                    {
                        secondNaluSize = i - secondStartCodeIndex;
                    }
                    if ((data[i+4] & 0x1F) == 5) //IFrame
                    {
                        thirdStartCodeIndex = i;
                        break;
                    }
                }
            }

            int thirdNaluSize = 0;
            int counter = thirdStartCodeIndex + 4;
            while (counter++ < data.length - 1)
            {
                if (data[counter] == 0x00 && data[counter + 1] == 0x00 && data[counter + 2] == 0x00 && data[counter + 3] == 0x01)
                {
                    thirdNaluSize = counter - thirdStartCodeIndex;
                    break;
                }
            }

            // This is how you would remove the "\x00\x00\x00\x01"
            //  byte[] firstNalu = new byte[firstNaluSize - 4];
            //  byte[] secondNalu = new byte[secondNaluSize - 4];
            //  byte[] thirdNalu = new byte[thirdNaluSize];

            //  System.arraycopy(data, thirdStartCodeIndex, thirdNalu, 0, thirdNaluSize);
            //  System.arraycopy(data, firstStartCodeIndex+4, firstNalu, 0, firstNaluSize-4);
            //  System.arraycopy(data, secondStartCodeIndex+4, secondNalu, 0, secondNaluSize-4);

            byte[] firstNalu = new byte[firstNaluSize];
            byte[] secondNalu = new byte[secondNaluSize];
            byte[] thirdNalu = new byte[thirdNaluSize];

            System.arraycopy(data, thirdStartCodeIndex, thirdNalu, 0, thirdNaluSize);
            System.arraycopy(data, firstStartCodeIndex, firstNalu, 0, firstNaluSize);
            System.arraycopy(data, secondStartCodeIndex, secondNalu, 0, secondNaluSize);

            sps_params = firstNalu;
            pps_params = secondNalu;
            i_frame = thirdNalu;

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getSPS () {
        return sps_params;
    }

    public byte[] getPPS () {
        return pps_params;
    }

    public byte[] nextFrame () {
        return i_frame;
    }

    public void release () {
        // Logout of server;
    }

    /*
    ** Simple function to return a byte array from an input stream
    */
    private byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer,0,read);
        }
        out.close();
        return out.toByteArray();
    }
}
