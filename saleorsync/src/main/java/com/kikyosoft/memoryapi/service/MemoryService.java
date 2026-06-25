package com.kikyosoft.memoryapi.service;
import com.kikyosoft.memoryapi.model.MemoryRequest;
import com.kikyosoft.memoryapi.model.MemoryResponse;
import org.springframework.stereotype.Service;


@Service
public class MemoryService {
	public MemoryResponse storeMemoryFromMessage(MemoryRequest request) {
		// 1. Call OpenAI to summarize the message
		// 2. Embed the summary
		// 3. Store embedding + summary in Postgres
		// 4. Return response


		// Stubbed response:
		MemoryResponse res = new MemoryResponse();
		res.success = true;
		res.summary = "The user said he loves Diana.";
		res.memoryId = "mem_1234";
		return res;
	}


	public MemoryResponse recallMemoryFromPrompt(MemoryRequest request) {
		// 1. Embed the user's query
		// 2. Search `memory_vectors` table
		// 3. Return best-matching summary


		MemoryResponse res = new MemoryResponse();
		res.success = true;
		res.summary = "You once asked me if I love Diana and said you love her.";
		res.matchInfo = "Matched memory from Sept 12, 2025.";
		return res;
	}
}
