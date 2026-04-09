package com.partition.domain.supply.service;

import com.partition.domain.supply.dto.response.ReceiptAnalysisResponse;
import com.partition.domain.supply.dto.response.ReceiptItemResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptAnalysisService {

    @Value("${fastapi.url}")
    private String fastapiUrl;

    private final RestTemplate restTemplate;

    public ReceiptAnalysisResponse analyzeReceipt(MultipartFile image) {
        validateImage(image);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", resource);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    fastapiUrl + "/api/receipts/recognize",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return parseResponse(response.getBody());

        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패", e);
            throw new CustomException(SupplyErrorCode.SUPPLY_4003);
        } catch (HttpClientErrorException e) {
            log.error("FastAPI 호출 실패 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw new CustomException(SupplyErrorCode.SUPPLY_4004);
            }
            throw new CustomException(SupplyErrorCode.SUPPLY_4003);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("영수증 분석 중 오류 발생", e);
            throw new CustomException(SupplyErrorCode.SUPPLY_4003);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4001);
        }

        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4002);
        }

        String extension = originalFilename.toLowerCase();
        if (!extension.endsWith(".jpg") && !extension.endsWith(".jpeg") && !extension.endsWith(".png")) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4002);
        }
    }

    @SuppressWarnings("unchecked")
    private ReceiptAnalysisResponse parseResponse(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4003);
        }

        Boolean success = (Boolean) responseBody.get("success");
        if (!Boolean.TRUE.equals(success)) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4003);
        }

        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) responseBody.get("items");
        if (rawItems == null || rawItems.isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4004);
        }

        boolean hasValidItem = rawItems.stream()
                .anyMatch(item -> item != null && item.values().stream().anyMatch(v -> v != null));
        if (!hasValidItem) {
            throw new CustomException(SupplyErrorCode.SUPPLY_4004);
        }

        List<ReceiptItemResponse> items = rawItems.stream()
                .map(this::mapToReceiptItem)
                .toList();

        return ReceiptAnalysisResponse.builder()
                .items(items)
                .build();
    }

    private ReceiptItemResponse mapToReceiptItem(Map<String, Object> item) {
        return ReceiptItemResponse.builder()
                .itemName((String) item.get("item_name"))
                .purchaseDate((String) item.get("purchaseDate"))
                .amount(parseIntOrNull(item.get("amount")))
                .quantity(parseIntOrNull(item.get("quantity")))
                .category((String) item.get("parent_category"))
                .subCategory((String) item.get("enum"))
                .build();
    }

    private Integer parseIntOrNull(Object value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}