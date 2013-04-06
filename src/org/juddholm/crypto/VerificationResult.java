package org.juddholm.crypto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.juddholm.mixnet.enums.VerificationStatus;
import org.omg.CORBA.Environment;

public class VerificationResult implements Serializable {
	private VerificationStatus status = VerificationStatus.PASS;
	private List<String> messages = new ArrayList<String>();
	
	public VerificationResult()
	{
		
	}
	
	public List<String> getMessages() {
		return messages;
	}
	public void addMessage(String message) {
		this.messages.add(message);
	}
	
	public void addErrorMessage(String message)
	{
		this.status = VerificationStatus.ERROR;
		addMessage(message);
	}
	
	public VerificationStatus getStatus() {
		return status;
	}
	
	public int messageCount() {
		return messages.size();
	}
	
	public void setStatus(VerificationStatus status) {
		this.status = status;
	}
	
	public void mergeWith(VerificationResult result)
	{
		this.messages.addAll(result.messages);
		this.status = (status.ordinal() > result.status.ordinal()) ? this.status : result.status;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Verification: ").append(status).append("\n");
		builder.append("Messages (").append(messageCount()).append("):").append("\n");
		int i = 0;
		for(String message : messages)
			builder.append("#").append(i++).append(" ").append(message).append("\n");
		return builder.toString();
		
	}
}

