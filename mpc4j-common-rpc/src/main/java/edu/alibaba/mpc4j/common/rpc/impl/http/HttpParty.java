package edu.alibaba.mpc4j.common.rpc.impl.http;

import edu.alibaba.mpc4j.common.rpc.Party;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class HttpParty implements Party {
    
    private final int partyId;

    private final String partyName;

    private final String partyAddress;

    HttpParty(int partyId, String partyName, String partyAddress) {
        Preconditions.checkArgument(partyId >= 0, "Party ID must be greater than 0");
        Preconditions.checkArgument(StringUtils.isNotBlank(partyName), "Party Name should not be blank");
        // TODO: check address
        this.partyId = partyId;
        this.partyName = partyName;
        this.partyAddress = partyAddress;
    }

    @Override
    public int getPartyId() {
        return partyId;
    }

    @Override
    public String getPartyName() {
        return partyName;
    }

    public String getPartyAddress() {
        return partyAddress;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(partyId)
            .append(partyName)
            .append(partyAddress)
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HttpParty)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        HttpParty that = (HttpParty)obj;
        return new EqualsBuilder()
            .append(this.partyId, that.partyId)
            .append(this.partyName, that.partyName)
            .append(this.partyAddress, that.partyAddress)
            .isEquals();
    }

    @Override
    public String toString() {
        return String.format("%s (ID = %s, path = %s)", partyName, partyId, partyAddress);
    }
}
