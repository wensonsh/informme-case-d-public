package de.dh.informme.hl7Mock;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.datatype.CX;
import ca.uhn.hl7v2.model.v26.message.ADT_A01;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class Hl7MockService {

    @Autowired
    private Hl7MockRepository hl7MockRepository;

    /***
     * get a HL7 message by its MSH id
     *
     * @param mshId MSH id to look for
     * @return HL7 message
     */
    public Hl7Mock getByMshId(String mshId) {
        return hl7MockRepository.findByMshId(mshId).orElse(null);
    }

    /**
     * update a given HL7 message
     *
     *
     * @param hl7MessageAsString    HL7 message
     * @param newPatientIdentifier  new patient identifier
     * @return message updated or message saved
     * @throws HL7Exception is thrown when there was an exception while parsing the message
     */
    public String updateMessage(String hl7MessageAsString, String newPatientIdentifier) throws HL7Exception {
        HapiContext context = new DefaultHapiContext();
        Parser parser = context.getPipeParser();
        Message message = parser.parse(hl7MessageAsString);

        if (message instanceof ADT_A01 adtMessage) {
            MSH msh = adtMessage.getMSH();
            updatePatientIdentifierList(adtMessage.getPID(), newPatientIdentifier);
            msh.getDateTimeOfMessage().setValue(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            String updatedMessage = parser.encode(adtMessage);
            String processingId = msh.getMessageControlID().getValue();
            if (getByMshId(processingId) != null) {
                Hl7Mock hl7Mock = getByMshId(processingId);
                hl7Mock.setMessage(updatedMessage);
                hl7MockRepository.saveAndFlush(hl7Mock);
                return "Message updated";
            } else {
                Hl7Mock hl7Mock = new Hl7Mock(processingId, updatedMessage);
                hl7MockRepository.save(hl7Mock);
                return "Message saved";
            }
        }
        return null;
    }

    /**
     * update the patient identifier list
     *
     * @param pid           PID segment
     * @param newIdentifier new patient identifier
     * @throws DataTypeException is thrown when there was an exception while parsing the message
     */
    public void updatePatientIdentifierList(PID pid, String newIdentifier) throws DataTypeException {
        int nextIdentifierIndex = pid.getPatientIdentifierList().length;
        CX newPatientIdentifier = pid.getPatientIdentifierList(nextIdentifierIndex);
        newPatientIdentifier.getIDNumber().setValue(newIdentifier);
        newPatientIdentifier.getAssigningAuthority().getNamespaceID().setValue("Praxis Digital Health");
    }

    /**
     * get a random HL7Mock out of the database
     *
     * @return random HL7Mock object
     */
    public Hl7Mock getRandomHl7Mock() {
        Hl7Mock randomHl7Mock = hl7MockRepository.findRandom();
        if (randomHl7Mock != null) {
            return randomHl7Mock;
        } else {
            return hl7MockRepository.save(getDefaultTestHl7Mock());
        }
    }

    /**
     * get a HL7Mock
     *
     * @return HL7Mock object
     */
    private Hl7Mock getDefaultTestHl7Mock() {
        return new Hl7Mock("123456", "MSH|^~\\&|SendingApplication|SendingFacility|ReceivingApplication|ReceivingFacility|202401041230||ADT^A01|123456|P|2.6\n" +
                "EVN|A01|202401041230|||\n" +
                "PID|1||123456789^^^Hospital^MR||Max^Mustermann^^^Herr||19900101|M|||Mockstreet 1^^Mockcity^Mockstate^12345^Deutschland||^01231234567^PRN^max.mustermann@mail.de^49^123^1234567^^^^^^||||||\n" +
                "PV1|1|I|2000^2050^01||||12345^Doe^Jane^A^^Dr.^MD|67890^Musterfrau^Mia^B^^Dr.^MD||||||||||1234567890||||||||||||||202401041230||");
    }
}
