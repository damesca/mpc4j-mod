package edu.alibaba.mpc4j.common.rpc.impl.http;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.RpcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class HttpRpcManager implements RpcManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRpcManager.class);
    /**
     * 参与方数量
     */
    private final int partyNum;
    /**
     * 参与方集合
     */
    private final Set<HttpParty> httpPartySet;
    /**
     * 所有参与方RPC
     */
    private final Map<Integer, HttpRpc> httpRpcMap;

    /**
     * 初始化内存通信管理器。
     *
     * @param partyNum 参与方数量。
     */
    public HttpRpcManager(int partyNum, List<String> partyAddresses, String partyUri) {
        Preconditions.checkArgument(partyNum > 1, "Number of parties must be greater than 1");
        this.partyNum = partyNum;
        // 初始化所有参与方
        httpPartySet = new HashSet<>(partyNum);
        IntStream.range(0, partyNum).forEach(partyId -> {
            HttpParty httpParty = new HttpParty(partyId, getPartyName(partyId), partyAddresses.get(partyId));
            httpPartySet.add(httpParty);
        });
        // 初始化所有参与方的内存通信
        httpRpcMap = new HashMap<>(partyNum);
        for (HttpParty httpParty : httpPartySet) {
            HttpRpc httpRpc = new HttpRpc(httpParty, httpPartySet, partyUri);
            httpRpcMap.put(httpRpc.ownParty().getPartyId(), httpRpc);
            LOGGER.debug("Add http party: {}", httpParty);
        }
    }

    @Override
    public Rpc getRpc(int partyId) {
        Preconditions.checkArgument(
            partyId >= 0 && partyId < partyNum, "Party ID must be in range [0, %s)", partyNum
        );
        return httpRpcMap.get(partyId);
    }

    private String getPartyName(int partyId) {
        return "P_" + (partyId + 1);
    }

    @Override
    public int getPartyNum() {
        return partyNum;
    }

    @Override
    public Set<Party> getPartySet() {
        return new HashSet<>(httpPartySet);
    }
}
