package de.dh.informme.doctorsOffice.hl7;

import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v26.datatype.SAD;
import ca.uhn.hl7v2.model.v26.datatype.XAD;
import ca.uhn.hl7v2.model.v26.datatype.XTN;
import ca.uhn.hl7v2.model.v26.segment.PID;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class Hl7Parser {

    /**
     * get the patient's first name
     *
     * @param pid PID segment of the HL7 message
     * @return patient's first name
     */
    public String getPatientFirstName(PID pid) {
        return pid.getPatientName(0).getGivenName().getValue();
    }

    /**
     * get the patient's last name
     *
     * @param pid PID segment of the HL7 message
     * @return patient's last name
     */
    public String getPatientLastName(PID pid) {
        return pid.getPatientName(0).getFamilyName().getSurname().getValue();
    }

    /**
     * get the patient's address as String
     *
     * @param pid PID segment of the HL7 message
     * @return patient's address as String
     */
    public String getPatientAddressAsString(PID pid) {
        XAD[] addresses = pid.getPatientAddress();
        String addressAsString = "Invalid";
        if (addresses == null || addresses.length == 0) {
            return addressAsString;
        }
        XAD address = addresses[0];
        String streetAndHouseNumber = getStreetAndDwellingNumber(address);
        String city = address.getCity().toString();
        String country = address.getCountry().toString();
        String postalCode = address.getZipOrPostalCode().toString();

        if (streetAndHouseNumber != null && postalCode != null && city != null) {
           addressAsString = streetAndHouseNumber + ", " + postalCode + " " + city + (country != null? ", " + country : "");
        }
        return addressAsString;
    }

    /**
     * get the patient's administrative sex
     *
     * @param pid PID segment of the HL7 message
     * @return patient's administrative sex
     */
    public String getPatientAdministrativeSex(PID pid) {
        return pid.getAdministrativeSex().getValue();
    }

    /**
     * get the patient's birthday
     *
     * @param pid PID segment of the HL7 message
     * @return patient's birthday
     * @throws ParseException
     */
    public Date getPatientBirthday(PID pid) throws ParseException {
        return convertDate(pid.getDateTimeOfBirth().getValue());
    }

    /**
     * get the patient's telephone number
     *
     * @param pid PID segment of the HL7 message
     * @return patient's telephone number
     */
    public String getPatientTelephoneNumber(PID pid) {
        XTN[] phoneNumbersHome = pid.getPhoneNumberHome();
        XTN[] phoneNumbersBusiness = pid.getPhoneNumberBusiness();

        if (phoneNumbersHome.length > 0) {
            String countryCode = phoneNumbersHome[0].getCountryCode().getValue() != null? phoneNumbersHome[0].getCountryCode().getValue() : "";
            String vorwahl = phoneNumbersHome[0].getAreaCityCode().getValue() != null? phoneNumbersHome[0].getAreaCityCode().getValue() : "";
            String number = phoneNumbersHome[0].getLocalNumber().getValue() != null? phoneNumbersHome[0].getLocalNumber().getValue() : "";
            return "+" + countryCode + " " + vorwahl + " " + number;
        } else if (phoneNumbersBusiness.length > 0) {
            String countryCode = phoneNumbersBusiness[0].getCountryCode().getValue() != null? phoneNumbersHome[0].getCountryCode().getValue() : "";
            String vorwahl = phoneNumbersBusiness[0].getAreaCityCode().getValue() != null? phoneNumbersHome[0].getAreaCityCode().getValue() : "";
            String number = phoneNumbersBusiness[0].getLocalNumber().getValue() != null? phoneNumbersHome[0].getLocalNumber().getValue() : "";
            return countryCode + " " + vorwahl + " " + number;
        }
        return null;
    }

    /**
     * get the patient's email address
     *
     * @param pid PID segment of the HL7 message
     * @return patient's email address
     */
    public String getPatientEmail(PID pid) {
        if (pid.getPhoneNumberHome().length > 0) {
            if (pid.getPhoneNumberHome()[0].getCommunicationAddress() != null) {
                return pid.getPhoneNumberHome()[0].getCommunicationAddress().toString();
            }
        }
        return null;
    }

    /**
     * convert a date from String to Date
     *
     * @param date date as String
     * @return date as Date
     * @throws ParseException is thrown when there was an exception while parsing the message
     */
    private Date convertDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.parse(date);
    }

    /**
     * get street and dwelling number from address
     *
     * @param address address of the patient
     * @return street and dwelling number as String
     */
    private String getStreetAndDwellingNumber(XAD address) {
        String streetAndHouseNumber = null;
        SAD streetAddress = address.getStreetAddress();
        if (streetAddress != null) {
            Type[] components = streetAddress.getComponents();
            if (components == null || components.length == 0) {
                return null;
            }
            streetAndHouseNumber = components[0].toString();
        }
        return streetAndHouseNumber;
    }
}
