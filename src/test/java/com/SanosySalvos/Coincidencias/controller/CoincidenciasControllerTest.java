package com.SanosySalvos.Coincidencias.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.SanosySalvos.Coincidencias.dto.AnalisisRequestDTO;
import com.SanosySalvos.Coincidencias.model.Coincidencias;
import com.SanosySalvos.Coincidencias.model.EstadoCoincidencia;
import com.SanosySalvos.Coincidencias.service.CoincidenciasService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CoincidenciasController.class)
class CoincidenciasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoincidenciasService coincidenciasService;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON fácilmente

    @Test
    void analizarMatch_DeberiaRetornar200() throws Exception {
        AnalisisRequestDTO request = new AnalisisRequestDTO();
        
        mockMvc.perform(post("/api/coincidencias/analizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Análisis procesado con éxito."));
    }

    @Test
    void descartarCoincidencia_DeberiaRetornar200ConCoincidencia() throws Exception {
        Coincidencias c = new Coincidencias();
        c.setId(1L);
        c.setEstado(EstadoCoincidencia.DESCARTADO);

        when(coincidenciasService.descartarCoincidencia(1L)).thenReturn(c);

        mockMvc.perform(put("/api/coincidencias/1/descartar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("DESCARTADO"));
    }
}