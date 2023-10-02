package edu.alibaba.mpc4j.common.rpc.impl.http;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.impl.http.HttpPtoDesc.StepEnum;
import edu.alibaba.mpc4j.common.rpc.utils.DataPacket;
import edu.alibaba.mpc4j.common.rpc.utils.DataPacketHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.InterruptedException;

public class HttpRpc implements Rpc {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRpc.class);

    private static final String ENDPOINT_SEND = "/send";

    private static final String ENDPOINT_RECEIVE = "/receive";

    private static final String MESSAGE_PASSING_BIN = "0x608060405234801561001057600080fd5b5060405161027c38038061027c833981810160405281019061003291906101b7565b505061022f565b6000604051905090565b600080fd5b600080fd5b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6100a082610057565b810181811067ffffffffffffffff821117156100bf576100be610068565b5b80604052505050565b60006100d2610039565b90506100de8282610097565b919050565b600067ffffffffffffffff8211156100fe576100fd610068565b5b61010782610057565b9050602081019050919050565b60005b83811015610132578082015181840152602081019050610117565b83811115610141576000848401525b50505050565b600061015a610155846100e3565b6100c8565b90508281526020810184848401111561017657610175610052565b5b610181848285610114565b509392505050565b600082601f83011261019e5761019d61004d565b5b81516101ae848260208601610147565b91505092915050565b600080604083850312156101ce576101cd610043565b5b600083015167ffffffffffffffff8111156101ec576101eb610048565b5b6101f885828601610189565b925050602083015167ffffffffffffffff81111561021957610218610048565b5b61022585828601610189565b9150509250929050565b603f8061023d6000396000f3fe6080604052600080fdfea2646970667358221220fa9aa6d21373c78c2e82dd0ef5fcb9b7eb3d0833b36cd0b95eebd14b50a92f1464736f6c634300080b0033";

    private static final int DEFAULT_READ_WAIT_MILLI_SECOND = 1;

    private static final int DEFAULT_DELETE_WAIT_MILLI_SECOND = 10;

    private static final String FILE_NAME_SEPARATOR = "_";

    private static final String FILE_STATUS_SUFFIX = "STATUS";

    private static final String FILE_PAYLOAD_SUFFIX = "PAYLOAD";

    private final HashMap<Integer, HttpParty> partyIdHashMap;

    private final HttpParty ownParty;

    private long dataPacketNum;

    private long payloadByteLength;

    private long sendByteLength;

    private HttpClient client;

    private String myURI;

    public HttpRpc(HttpParty ownParty, Set<HttpParty> partySet, String myURI) {
        Preconditions.checkArgument(partySet.size() > 1, "Party set size must be greater than 1");
        Preconditions.checkArgument(partySet.contains(ownParty), "Party set must contain own party");
        this.ownParty = ownParty;
        partyIdHashMap = new HashMap<>();
        partySet.forEach(partySpec -> partyIdHashMap.put(partySpec.getPartyId(), partySpec));
        dataPacketNum = 0;
        payloadByteLength = 0;
        sendByteLength = 0;
        client = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.myURI = myURI;
    }

    @Override
    public Party ownParty() {
        return ownParty;
    }

    @Override
    public Set<Party> getPartySet() {
        return partyIdHashMap.keySet().stream().map(partyIdHashMap::get).collect(Collectors.toSet());
    }

    @Override
    public Party getParty(int partyId) {
        assert (partyIdHashMap.containsKey(partyId));
        return partyIdHashMap.get(partyId);
    }

    @Override
    public void connect() {
        int ownPartyId = ownParty.getPartyId();
        partyIdHashMap.keySet().stream().sorted().forEach(otherPartyId -> {
            if (otherPartyId != ownPartyId) {
                LOGGER.debug(
                    "{} successfully make connection with {}",
                    partyIdHashMap.get(ownPartyId), partyIdHashMap.get(otherPartyId)
                );
            }
        });
        LOGGER.info("{} connected", ownParty);
    }

    /*
     * send: 
     *  - header
     *  - payload
     *  - from
     *  - to
    */
    @Override
    public void send(DataPacket dataPacket) {
        DataPacketHeader headerObject = dataPacket.getHeader();
        String headerHash = "" + headerObject.hashCode();
        List<byte[]> payload = dataPacket.getPayload();
        int payloadStringNumber = 0;
        JSONObject jo = new JSONObject();

        jo.put("header", headerHash);

        // Convert the payload byteArrays to Base64 Strings and put into JSON
        List<String> jsonPayload = new ArrayList<String>();
        for (byte[] byteArray : payload) {
            payloadByteLength += byteArray.length;
            String payloadString = Base64.getEncoder().encodeToString(byteArray);
            sendByteLength = payloadString.getBytes(StandardCharsets.UTF_8).length;
            jsonPayload.add(payloadString);
        }
        jo.put("payload", jsonPayload);

        List<String> ownPartyStr = new ArrayList<String>();

        // Add a (recipients, [addr1, ..., addrN]) item in the json
        List<String> recipients = new ArrayList<String>();
        partyIdHashMap.forEach((key, party) -> {
            if(party.getPartyId() != ownParty.getPartyId()) {
                recipients.add(party.getPartyAddress());
            }else{
                ownPartyStr.add(party.getPartyAddress());
            }
        });
        jo.put("recipients", recipients);

        /*LOG*/System.out.println("[HttpRpc] send");
        /*LOG*/System.out.println(jo.toString(0));

        /*LOG*/System.out.println("OWN PARTY");
        partyIdHashMap.forEach((key, party) -> {
            if(party.getPartyId() == ownParty.getPartyId()) {
                /*LOG*/System.out.println(party.getPartyAddress());
            }
        });

        // POST json message to the hardcoded ENDPOINT
        HttpResponse<String> response = postJson(
            jo.toString(0),
            this.myURI + ENDPOINT_SEND
        );

        /*LOG*/System.out.println(response.body());

        /*
         * - Send message passing transaction to EthSigner to notify packet header
         * - Must send both (header, tx_hash)
        */ 

        /*LOG*/System.out.println("[HttpRpc] sending hash through message passing...");

        int i = 64 - headerHash.length();
        String hexHeader = "";
        for(int j = 0; j < i; j++){
            hexHeader += "0";
        }
        hexHeader += headerHash;

        String base64Hash = response.body().substring(8, response.body().length()-2);
        i = 64 - base64Hash.length(); /* TODO: revise */
        String hexHash = "";
        for(int j = 0; j < i; j++) {
            hexHash += "0";
        }
        hexHash += base64Hash;
        /*LOG*/System.out.println("HEXHASH");
        /*LOG*/System.out.println(hexHash);

        String headerLength = "";
        if(hexHeader.length() > 9) {
            headerLength += "00000000000000000000000000000000";
            headerLength += "000000000000000000000000000000";
            headerLength += hexHeader.length();
        } else {
            headerLength += "00000000000000000000000000000000";
            headerLength += "0000000000000000000000000000000";
            headerLength += hexHeader.length();
        }

        String hashLength = "";
        if(hexHash.length() > 9) {
            hashLength += "00000000000000000000000000000000";
            hashLength += "000000000000000000000000000000";
            hashLength += hexHash.length();
        } else {
            hashLength += "00000000000000000000000000000000";
            hashLength += "0000000000000000000000000000000";
            hashLength += hexHash.length();
        }
        
        String encodedAbi = MESSAGE_PASSING_BIN
            + "00000000000000000000000000000000"
            + "00000000000000000000000000000040"
            + "00000000000000000000000000000000"
            + "00000000000000000000000000000080"
            + headerLength
            + hexHeader
            + hashLength
            + hexHash;

        /*LOG*/System.out.println("ENCODED ABI");
        /*LOG*/System.out.println(encodedAbi);

        String privateFor = "";
        for (int j = 0; j < recipients.size(); j++) {
            privateFor += "\"";
            privateFor += recipients.get(j);
            if(j == recipients.size()-1){
                privateFor += "\"";
            }else{
                privateFor += "\",";
            }
        }

        String messagePassBody = "{\"jsonrpc\":\"2.0\","
            + "\"method\":\"eea_sendTransaction\","
            + "\"params\":[{"
            + "\"from\":" + "\"0xfe3b557e8fb62b89f4916b721be55ceb828dbd73\"" + ","
            + "\"data\":\"" + encodedAbi + "\","
            + "\"privateFrom\":\"" + ownPartyStr.get(0) + "\","
            + "\"privateFor\":[" + privateFor + "],"
            + "\"restriction\":\"restricted\""
            + "}],"
            + "\"id\":1"
            + "}";

        response = postJson(
            messagePassBody,
            "http://127.0.0.1:8590"
        );

        /*LOG*/System.out.println(response.body());

    }

    @Override
    public DataPacket receive(DataPacketHeader header) {

        String headerHash = "" + header.hashCode();
        
        /*LOG*/System.out.println("[HttpRpc] receive - header");
        /*LOG*/System.out.println(headerHash);

        /*
         * - Receive from events the tx_hash querying the header
         * - Use the tx_hash to retrieve a message from Tessera
        */

        JSONObject jo = new JSONObject();
        jo.put("header", headerHash);

        // POST json message to the hardcoded ENDPOINT (blocking until correct reception)
        HttpResponse<String> response = postJson(
            jo.toString(0),
            this.myURI + ENDPOINT_RECEIVE
        );

        if(response.statusCode() != 200) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                /*LOG*/System.out.println("InterruptedException");
            }
            response = postJson(
                jo.toString(0),
                this.myURI + ENDPOINT_RECEIVE
            );
        }

        // Receive content as JSON
        String resbody = response.body();
        /*LOG*/System.out.println("[HttpRpc] receive - body");
        /*LOG*/System.out.println(resbody);
        JSONObject jRes = new JSONObject(resbody);

        List<byte[]> byteArrayData = new LinkedList<>();
        JSONArray jsonPayload = jRes.getJSONArray("payload");
        for (Object payloadString : jsonPayload) {
            byte[] byteArray = Base64.getDecoder().decode(String.valueOf(payloadString));
            byteArrayData.add(byteArray);
        }

        return DataPacket.fromByteArrayList(header, byteArrayData);
    }

    @Override
    public long getPayloadByteLength() {
        return payloadByteLength;
    }

    @Override
    public long getSendByteLength() {
        return sendByteLength;
    }

    @Override
    public long getSendDataPacketNum() {
        return dataPacketNum;
    }

    @Override
    public void reset() {
        payloadByteLength = 0;
        sendByteLength = 0;
        dataPacketNum = 0;
    }

    @Override
    public void synchronize() {
        // 对参与方进行排序，所有在自己之前的自己作为client、所有在自己之后的自己作为server
        int ownPartyId = ownParty.getPartyId();
        partyIdHashMap.keySet().stream().sorted().forEach(otherPartyId -> {
            if (otherPartyId < ownPartyId) {
                // 如果对方排序比自己小，则自己是client，需要给对方发送同步信息
                DataPacketHeader clientSynchronizeHeader = new DataPacketHeader(
                    Long.MAX_VALUE - ownPartyId, HttpPtoDesc.getInstance().getPtoId(), StepEnum.CLIENT_SYNCHRONIZE.ordinal(),
                    ownPartyId, otherPartyId
                );
                send(DataPacket.fromByteArrayList(clientSynchronizeHeader, new LinkedList<>()));
                // 获得对方的回复
                DataPacketHeader serverSynchronizeHeader = new DataPacketHeader(
                    Long.MAX_VALUE - otherPartyId, HttpPtoDesc.getInstance().getPtoId(), StepEnum.SERVER_SYNCHRONIZE.ordinal(),
                    otherPartyId, ownPartyId
                );
                receive(serverSynchronizeHeader);
            } else if (otherPartyId > ownPartyId) {
                // 如果对方排序比自己大，则自己是server
                DataPacketHeader clientSynchronizeHeader = new DataPacketHeader(
                    Long.MAX_VALUE - otherPartyId, HttpPtoDesc.getInstance().getPtoId(), StepEnum.CLIENT_SYNCHRONIZE.ordinal(),
                    otherPartyId, ownPartyId
                );
                receive(clientSynchronizeHeader);
                DataPacketHeader serverSynchronizeHeader = new DataPacketHeader(
                    Long.MAX_VALUE - ownPartyId, HttpPtoDesc.getInstance().getPtoId(), StepEnum.SERVER_SYNCHRONIZE.ordinal(),
                    ownPartyId, otherPartyId
                );
                send(DataPacket.fromByteArrayList(serverSynchronizeHeader, new LinkedList<>()));
            }
        });
        LOGGER.info("{} synchronized", ownParty);
    }

    @Override
    public void disconnect() {
        LOGGER.info("{} disconnected", ownParty);
    }

    private HttpResponse<String> postJson(
            final String content,
            final String endpoint) {
        /*LOG*/System.out.println("[HttpRpc] postJson");
        /*LOG*/System.out.println(content);
        /*LOG*/System.out.println(endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(content))
                .build();
        try{
            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            return response;
        } catch(IOException e) {
            /*LOG*/System.out.println("IOException");
            return null;
        } catch(InterruptedException e) {
            /*LOG*/System.out.println("InterruptedException");
            return null;
        }
    }
}