package com.collab.version_service.controller;

import com.collab.version_service.model.DocumentVersion;
import com.collab.version_service.repository.VersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VersionController.class)
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VersionRepository versionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private DocumentVersion testVersion;

    @BeforeEach
    void setUp() {
        testVersion = new DocumentVersion();
        testVersion.setId(1L);
        testVersion.setDocumentId(1L);
        testVersion.setContent("Version 1 content");
        testVersion.setEditedByUserId(1L);
        testVersion.setTimestamp(LocalDateTime.now());
        testVersion.setVersionNumber(1);
    }

    @Test
    void testSaveVersion_Success() throws Exception {
        when(versionRepository.save(any(DocumentVersion.class))).thenReturn(testVersion);

        mockMvc.perform(post("/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVersion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Version saved successfully"))
                .andExpect(jsonPath("$.version.id").value(1L));

        verify(versionRepository, times(1)).save(any(DocumentVersion.class));
    }

    @Test
    void testGetVersions() throws Exception {
        List<DocumentVersion> versions = Arrays.asList(testVersion);
        when(versionRepository.findByDocumentId(1L)).thenReturn(versions);

        mockMvc.perform(get("/versions/document/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId").value(1L));
    }

    @Test
    void testGetVersion_Success() throws Exception {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));

        mockMvc.perform(get("/versions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentId").value(1L));
    }

    @Test
    void testGetVersionHistory() throws Exception {
        List<DocumentVersion> versions = Arrays.asList(testVersion);
        when(versionRepository.findByDocumentId(1L)).thenReturn(versions);

        mockMvc.perform(get("/versions/document/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId").value(1L));
    }

    @Test
    void testRevertVersion_Success() throws Exception {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(versionRepository.findByDocumentId(1L)).thenReturn(Arrays.asList(testVersion));
        when(versionRepository.save(any(DocumentVersion.class))).thenReturn(testVersion);

        mockMvc.perform(post("/versions/revert/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.newVersion").exists());
    }

    @Test
    void testTrackContributions() throws Exception {
        List<DocumentVersion> versions = Arrays.asList(testVersion);
        when(versionRepository.findByDocumentId(1L)).thenReturn(versions);

        mockMvc.perform(get("/versions/document/1/contributions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1L))
                .andExpect(jsonPath("$.totalVersions").value(1));
    }

    @Test
    void testGetUserContributions() throws Exception {
        List<DocumentVersion> allVersions = Arrays.asList(testVersion);
        when(versionRepository.findAll()).thenReturn(allVersions);

        mockMvc.perform(get("/versions/user/1/contributions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].editedByUserId").value(1L));
    }
}

