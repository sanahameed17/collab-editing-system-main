package com.collab.document_service.controller;

import com.collab.document_service.model.Document;
import com.collab.document_service.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentRepository documentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setTitle("Test Document");
        testDocument.setContent("Test content");
        testDocument.setOwnerId(1L);
    }

    @Test
    void testCreateDocument_Success() throws Exception {
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        mockMvc.perform(post("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDocument)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Document created successfully"))
                .andExpect(jsonPath("$.document.id").value(1L));

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testGetDocument_Success() throws Exception {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    void testGetDocument_NotFound() throws Exception {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllDocuments() throws Exception {
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findAll()).thenReturn(documents);

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testUpdateDocument_Success() throws Exception {
        Document updatedDoc = new Document();
        updatedDoc.setTitle("Updated Title");
        updatedDoc.setContent("Updated content");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        mockMvc.perform(put("/documents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document updated successfully"));
    }

    @Test
    void testDeleteDocument_Success() throws Exception {
        when(documentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(documentRepository).deleteById(1L);

        mockMvc.perform(delete("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document deleted successfully"));

        verify(documentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetDocumentsByOwner() throws Exception {
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByOwnerId(1L)).thenReturn(documents);

        mockMvc.perform(get("/documents/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value(1L));
    }
}

