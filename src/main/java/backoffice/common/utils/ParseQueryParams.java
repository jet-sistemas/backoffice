package backoffice.common.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.BadRequestException;

// TODO: Refactor to use generic field to convert string to list of Long
public class ParseQueryParams {
    public static List<Long> validateAndParseCampaignIds(String campaignIdsStr, Integer maxResults) {
        if (campaignIdsStr == null || campaignIdsStr.trim().isEmpty()) {
            throw new BadRequestException("campaignIds é obrigatório");
        }
        
        try {
            List<Long> campaignIds = Arrays.stream(campaignIdsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .map(Long::valueOf)
                .collect(Collectors.toList());
            
            if (campaignIds.isEmpty()) {
                throw new BadRequestException("Nenhum ID de campanha válido fornecido");
            }
            
            if (campaignIds.size() > maxResults) {
                throw new BadRequestException("Máximo de " + maxResults + " campanhas por requisição");
            }
            
            return campaignIds;
        } catch (NumberFormatException e) {
            throw new BadRequestException("IDs de campanha inválidos: " + e.getMessage());
        }
    }
}
