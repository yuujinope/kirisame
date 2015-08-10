package nope.yuuji.kirisame.network.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tkpd_Eka on 8/7/2015.
 */
public class MultipartEntity {

    private class Model {
        List<String> params = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<byte[]> filesData = new ArrayList<>();
        List<String> filesName = new ArrayList<>();
        List<String> filesParams = new ArrayList<>();
    }

    public static final String BOUNDARY = "THISISBOUNDARY-_YUKARI_YAKUMO_-THISISBOUNDARY";
    private static final String CRLF = "\r\n";// Line separator required by multipart/form-data.
    public static final String UTF8 = "UTF-8";

    private Model model = new Model();
    private OutputStream outputStream;
    private PrintWriter printWriter;

    public final void setOutputStream(OutputStream outputStream) throws IOException{
        this.outputStream = outputStream;
        printWriter = new PrintWriter(new OutputStreamWriter(outputStream, UTF8), true);
        setParameters();
    }

    public final void addPart(String param, String value){
        model.params.add(param);
        model.values.add(value);
    }

    public final void addFile(String param,String fileName, byte[] file){
        model.filesParams.add(param);
        model.filesName.add(fileName);
        model.filesData.add(file);
    }

    private void setParameters() throws IOException {
        writeAllParams();
        sendAllFiles();
        // End of multipart/form-data.
        printWriter.append("--" + BOUNDARY + "--").append(CRLF).flush();
    }

    private void writeAllParams(){
        int totalParam = model.params.size();
        for (int i = 0; i < totalParam; i++) {
            writeParam(model.params.get(i), model.values.get(i));
        }
    }

    private void sendAllFiles()throws IOException{
        int totalFiles = model.filesName.size();
        for (int i = 0; i < totalFiles; i++) {
            sendFiles(model.filesParams.get(i), model.filesName.get(i), model.filesData.get(i));
        }
    }

    private void writeParam(String name, String value) {
        printWriter.append("--" + BOUNDARY).append(CRLF);
        printWriter.append("Content-Disposition: form-data; name=\"" + name + "\"").append(CRLF);
        printWriter.append("Content-Type: text/plain; charset=" + "UTF-8").append(CRLF);
        printWriter.append(CRLF).append(value).append(CRLF).flush();
    }

    private void sendFiles(String param, String name, byte[] data) throws IOException {
        printWriter.append("--" + BOUNDARY).append(CRLF);
        printWriter.append("Content-Disposition: form-data; name=\"" + param + "\"; filename=\"" + name + "\"").append(CRLF);
        printWriter.append("Content-Type: " + URLConnection.guessContentTypeFromName(name)).append(CRLF);
        printWriter.append("Content-Transfer-Encoding: binary").append(CRLF);
        printWriter.append(CRLF).flush();
        outputStream.write(data);
        printWriter.flush();
        printWriter.append(CRLF).flush();
    }
}
