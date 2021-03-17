package io.mosip.registration.processor.reprocessor.service.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.registration.processor.core.abstractverticle.EventDTO;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.exception.WorkflowActionException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.spi.eventbus.EventHandler;
import io.mosip.registration.processor.packet.storage.utils.PacketManagerService;
import io.mosip.registration.processor.reprocessor.service.WorkflowActionService;
import io.mosip.registration.processor.reprocessor.util.WebSubUtil;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class WorkflowActionServiceTest {
	/** The registration status service. */
	@Mock
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The packet manager service. */
	@Mock
	private PacketManagerService packetManagerService;

	/** The core audit request builder. */
	@Mock
	AuditLogRequestBuilder auditLogRequestBuilder;

	/** The web sub util. */
	@Mock
	WebSubUtil webSubUtil;

	@InjectMocks
	WorkflowActionService workflowActionService;


	private InternalRegistrationStatusDto registrationStatusDto;
	
	MosipEventBus mosipEventBus;

	@Before
	public void setUp()
			throws Exception {
		registrationStatusDto = new InternalRegistrationStatusDto();
		registrationStatusDto.setRegistrationId("10003100030001520190422074511");
		registrationStatusDto.setRegistrationType("NEW");
		registrationStatusDto.setRegistrationStageName("SecurezoneNotificationStage");
		ReflectionTestUtils.setField(workflowActionService, "hotListedTag", "test");
		ReflectionTestUtils.setField(workflowActionService, "resumeFromBeginningStage", "SecurezoneNotificationStage");
		Mockito.doNothing().when(registrationStatusService).updateRegistrationStatus(any(), any(),
				Mockito.any());
		Mockito.doNothing().when(registrationStatusService).updateRegistrationStatus(any(), any(),
				Mockito.any());

		Mockito.when(packetManagerService.deleteTags(any(), any())).thenReturn(true);
		Mockito.when(auditLogRequestBuilder.createAuditRequestBuilder(any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(null);
		mosipEventBus = new MosipEventBus() {

			@Override
			public void send(MessageBusAddress toAddress, MessageDTO message) {


			}

			@Override
			public Vertx getEventbus() {

				return null;
			}

			@Override
			public void consumeAndSend(MessageBusAddress fromAddress, MessageBusAddress toAddress,
					EventHandler<EventDTO, Handler<AsyncResult<MessageDTO>>> eventHandler) {


			}

			@Override
			public void consume(MessageBusAddress fromAddress,
					EventHandler<EventDTO, Handler<AsyncResult<MessageDTO>>> eventHandler) {


			}
		};
	}

	@Test
	public void testResumeProcessing() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING", mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeProcessingWorkflowActionException() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenReturn(null);

		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING", mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeProcessingTablenotAccessibleException() throws WorkflowActionException {
		TablenotAccessibleException tablenotAccessibleException = new TablenotAccessibleException(
				PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenThrow(tablenotAccessibleException);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING", mosipEventBus);

	}

	@Test
	public void testResumeProcessingAndRemoveHotlistedTag() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeProcessingAndRemoveHotlistedTagWithException() throws Exception {
		Mockito.when(packetManagerService.deleteTags(any(), any())).thenReturn(false);
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeProcessingAndRemoveHotlistedTagTablenotAccessibleException() throws WorkflowActionException {
		TablenotAccessibleException tablenotAccessibleException = new TablenotAccessibleException(
				PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenThrow(tablenotAccessibleException);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeProcessingAndRemoveHotlistedTagWorkflowActionException() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(null);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_PROCESSING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test
	public void testResumeFromBeginning() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeFromBeginningWorkflowActionException() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(null);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING", mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeFromBeginningTablenotAccessibleException() throws WorkflowActionException {
		TablenotAccessibleException tablenotAccessibleException = new TablenotAccessibleException(
				PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenThrow(tablenotAccessibleException);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING", mosipEventBus);

	}

	@Test
	public void testResumeFromBeginningAndRemoveHotlistedTag()
			throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeFromBeginningAndRemoveHotlistedTagWithException() throws Exception {
		Mockito.when(packetManagerService.deleteTags(any(), any())).thenReturn(false);
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeFromBeginningAndRemoveHotlistedTagWorkflowActionException() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(null);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testResumeFromBeginningAndRemoveHotlistedTagTablenotAccessibleException()
			throws WorkflowActionException {
		TablenotAccessibleException tablenotAccessibleException = new TablenotAccessibleException(
				PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenThrow(tablenotAccessibleException);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "RESUME_FROM_BEGINNING_AND_REMOVE_HOTLISTED_TAG",
				mosipEventBus);

	}
	@Test
	public void testStopProcessing() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "STOP_PROCESSING",
				mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testStopProcessingWorkflowActionException() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(null);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "STOP_PROCESSING", mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testStopProcessingTablenotAccessibleException() throws WorkflowActionException {
		TablenotAccessibleException tablenotAccessibleException = new TablenotAccessibleException(
				PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString()))
				.thenThrow(tablenotAccessibleException);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "STOP_PROCESSING", mosipEventBus);

	}

	@Test(expected = WorkflowActionException.class)
	public void testUnknownWorkflow() throws WorkflowActionException {
		Mockito.when(registrationStatusService.getRegistrationStatus(Mockito.any())).thenReturn(registrationStatusDto);
		List<String> workflowIds = new ArrayList<String>();
		workflowIds.add("10003100030001520190422074511");
		workflowActionService.processWorkflowAction(workflowIds, "test", mosipEventBus);

	}
}
