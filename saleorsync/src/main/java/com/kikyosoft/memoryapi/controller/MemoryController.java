package com.kikyosoft.memoryapi.controller;


import com.kikyosoft.memoryapi.model.MemoryRequest;
import com.kikyosoft.memoryapi.model.MemoryResponse;
import com.kikyosoft.memoryapi.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/memory")
public class MemoryController {


@Autowired
private MemoryService memoryService;


@PostMapping("/store")
public MemoryResponse storeMemory(@RequestBody MemoryRequest request) {
return memoryService.storeMemoryFromMessage(request);
}


@PostMapping("/recall")
public MemoryResponse recallMemory(@RequestBody MemoryRequest request) {
return memoryService.recallMemoryFromPrompt(request);
}
}
