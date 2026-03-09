package project.v1.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import project.common.database.Pageable;
import project.common.exceptions.MessageErrorEnum;
import project.common.exceptions.customs.NotFoundException;
import project.v1.dtos.agent.AgentDTO;
import project.v1.dtos.agent.AgentValidateDTO;
import project.v1.dtos.common.PageDTO;
import project.v1.entities.CharityAgent;
import project.v1.entities.enums.AgentStatusEnum;

@ApplicationScoped
public class AdminService {
  @Inject
  private AgentService agentService;

  public Pageable<AgentDTO> listAgents(AgentStatusEnum status, PageDTO pageDTO) {
    return agentService.listAgents(status, pageDTO);
  }

  @Transactional
  public void validateAgent(AgentValidateDTO dto, Long agentId) {
    CharityAgent agent = agentService.findById(agentId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.AGENT_NOT_FOUND.getMessage()));

    agent.setStatus(AgentStatusEnum.ACTIVE);
    agent.getUser().setIsActive(true);
  }
}
