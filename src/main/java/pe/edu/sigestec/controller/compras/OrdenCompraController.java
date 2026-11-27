package pe.edu.sigestec.controller.compras;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.compras.DetalleOrdenCompraRequestDTO;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.compras.OrdenCompraService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@RestController
@RequestMapping("/api/v1/ordenes-compra")
public class OrdenCompraController {
    private final OrdenCompraService service;
    private final RespuestaService respuestas;
    private final ArchivoStorageService storage;
    private final ObjectMapper mapper;
    public OrdenCompraController(OrdenCompraService s, RespuestaService respuestas, ArchivoStorageService st, ObjectMapper m) {
        service = s;
        this.respuestas = respuestas;
        storage = st;
        mapper = m;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::compra).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object registrar(@RequestParam Long cotizacionId, @RequestParam String numero,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmision,
                            @RequestParam BigDecimal montoTotal, @RequestPart("pdf") MultipartFile pdf,
                            @RequestParam(required = false) String detallesJson) {
        List<DetalleOrdenCompraRequestDTO> lineas = null;
        if (detallesJson != null && !detallesJson.isBlank()) try {
            lineas = mapper.readValue(detallesJson, new TypeReference<List<DetalleOrdenCompraRequestDTO>>() {
            });
        } catch (Exception e) {
            throw NegocioException.invalido("Detalle de OC no es JSON válido");
        }
        return respuestas.compra(service.registrar(cotizacionId, numero, fechaEmision, montoTotal, pdf, lineas));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','ADMIN')")
    public ResponseEntity<Resource> pdf(@PathVariable Long id) {
        OrdenCompra oc = service.buscarPorId(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + oc.getNumeroOrdenCompra() + ".pdf\"")
                .body(storage.cargar(oc.getUrlArchivo()));
    }
}
