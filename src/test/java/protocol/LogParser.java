package protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import client.ClientState;
import server.Domain;
import server.Sale;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LogParser {

    private class LogEvent {
        private String content;

        public LogEvent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        private boolean isStatus() {
            if (content.contains("[STATUS]")) {
                // this function removes the "INFO: [STATUS]" from the content
                this.content = content.substring(15);
                return true;
            }
            return false;
        }

        private boolean isInternal() {
            if (content.contains("[INTERNAL]")) {
                // this function removes the "INFO: " from the content
                this.content = content.substring(6);
                return true;
            }
            return false;
        }

        private boolean isSend() {
            if (content.contains("[SEND]")) {
                // this function removes the "INFO: [SEND]" from the content
                this.content = content.substring(13);
                return true;
            }
            return false;
        }

        private boolean isReceive() {
            if (content.contains("[RECEIVE]")) {
                // this function removes the "INFO: [RECEIVE]" from the content
                this.content = content.substring(16);
                return true;
            }
            return false;
        }
    }

    public static boolean compareRequestParameters(ProtocolCommand requestCommand, Map<String, Object> expectedParameters, Map<String, Object> actualParameters) {
        return switch (requestCommand) {
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER -> Arrays.equals((byte[]) expectedParameters.get("PublicKey"), (byte[]) actualParameters.get("PublicKey"));
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK -> Arrays.equals((byte[]) expectedParameters.get("PublicKey"), (byte[]) actualParameters.get("PublicKey"));
            case SIGN_UP -> expectedParameters.get("Mail").equals(actualParameters.get("Mail")) && expectedParameters.get("Name").equals(actualParameters.get("Name")) && expectedParameters.get("Pwd").equals(actualParameters.get("Pwd"));
            case SIGN_UP_OK -> expectedParameters.get("Mail").equals(actualParameters.get("Mail")) && expectedParameters.get("Name").equals(actualParameters.get("Name"));
            case SIGN_IN -> expectedParameters.get("Mail").equals(actualParameters.get("Mail")) && expectedParameters.get("Pwd").equals(actualParameters.get("Pwd")) && expectedParameters.get("SendDomainList") == actualParameters.get("SendDomainList");
            case SIGN_IN_OK -> expectedParameters.get("Name").equals(actualParameters.get("Name"));
            case DOMAINS_LIST_OK -> Arrays.equals((Domain[]) expectedParameters.get("Domains"), (Domain[]) actualParameters.get("Domains"));
            case SALES_FROM_DOMAIN -> expectedParameters.get("Domain").equals(actualParameters.get("Domain"));
            case SALES_FROM_DOMAIN_OK -> Arrays.equals((Sale[]) expectedParameters.get("AnnoncesFromDomain"), (Sale[]) actualParameters.get("AnnoncesFromDomain"));
            case CREATE_SALE -> expectedParameters.get("Title").equals(actualParameters.get("Title")) && expectedParameters.get("Descriptif").equals(actualParameters.get("Descriptif")) && expectedParameters.get("Domain").equals(actualParameters.get("Domain")) && expectedParameters.get("Price").equals(actualParameters.get("Price"));
            case CREATE_SALE_OK -> expectedParameters.get("Title").equals(actualParameters.get("Title"));
            case UPDATE_SALE -> (int) expectedParameters.get("Price") == (int) actualParameters.get("Price") && (int) expectedParameters.get("Id") == (int) actualParameters.get("Id") && expectedParameters.get("Title").equals(actualParameters.get("Title")) && expectedParameters.get("Descriptif").equals(actualParameters.get("Descriptif"));
            case DELETE_SALE -> (int) expectedParameters.get("Id") == (int) actualParameters.get("Id");
            case SIGN_OUT, DOMAINS_LIST, SIGN_OUT_OK, UPDATE_SALE_OK, DELETE_SALE_OK, UDP_SERVER_OK -> true;
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO, SIGN_UP_KO, SIGN_IN_KO, CREATE_SALE_KO, SIGN_OUT_KO, DELETE_SALE_KO, SALES_FROM_DOMAIN_KO, UPDATE_SALE_KO, DOMAINS_LIST_KO, UDP_SERVER_KO -> expectedParameters.get("Error").equals(actualParameters.get("Error"));
            default -> throw new UnsupportedOperationException("Unimplemented case: " + requestCommand);
        };
    }

    private void myAssert(boolean condition, String message) {
        if (!condition)
            System.out.println(message);
        assert condition;
    }

    public boolean checkLogFile(String logFile, Object... expectedLogEntries) {
        try{
            FileInputStream logFileInputStream = new FileInputStream(logFile);
            BufferedReader readerLogFile = new BufferedReader(new InputStreamReader(logFileInputStream));
            String logMessage;
            int indexLogEntries = 0;
            ArrayList<LogEvent> logEvents = new ArrayList<>();
            // first line is the date and the function where the log was called
            // second line is the log message
            while((readerLogFile.readLine()) != null && (logMessage = readerLogFile.readLine()) != null)
                logEvents.add(new LogEvent(logMessage));
            myAssert(logEvents.size() == expectedLogEntries.length, "Expected: " + expectedLogEntries.length + " Current : " + logEvents.size());
            for (LogEvent event: logEvents) {
                if (event.isStatus())
                    myAssert(expectedLogEntries[indexLogEntries] == ClientState.valueOf(event.getContent()), "Expected: " + expectedLogEntries[indexLogEntries] + " Current : " + event.getContent());
                else if (event.isInternal())
                    myAssert(expectedLogEntries[indexLogEntries].equals(event.getContent()), "Expected: " + expectedLogEntries[indexLogEntries] + " Current : " + event.getContent());
                else if (event.isSend() || event.isReceive()) {
                    Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
                    Request request = gson.fromJson(event.getContent(), Request.class);
                    myAssert(((Request) expectedLogEntries[indexLogEntries]).getCommand() == request.getCommand(), "At index " + indexLogEntries + ": Expected: " + ((Request) expectedLogEntries[indexLogEntries]).getCommand() + " Current: " + request.getCommand());
                    myAssert(compareRequestParameters(((Request) expectedLogEntries[indexLogEntries]).getCommand(), ((Request) expectedLogEntries[indexLogEntries]).getParams(), request.getParams()), "Expected: " + ((Request) expectedLogEntries[indexLogEntries]).getParams() + " Current : " + request.getParams());
                }
                indexLogEntries++;
            }
            logFileInputStream.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return true;
    }
}
