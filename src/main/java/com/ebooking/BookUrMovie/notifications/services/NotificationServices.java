package com.ebooking.BookUrMovie.notifications.services;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.Seat;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sun.mail.iap.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class NotificationServices {

    @Value("${endpoint}")
    private String endPoint;

    @Autowired
    private JavaMailSender javaMailSender;

    private RestTemplate restTemplate;

    @Value("${spring.mail.username}")
    private String sender;

    public Booking sendEmail(int id) throws NotFoundException, IOException, ConnectionException {
        log.debug("Sending Movie Ticket for booking id : " + id);
        restTemplate = CommunicationService.securityRestTemplateBuilder("admin@bym.com", "admin");

        ObjectMapper mapper = new ObjectMapper();

        Booking booking;
        try {
            booking = restTemplate.getForObject(endPoint + "/bookings/" + id, Booking.class);
        } catch (HttpClientErrorException exp) {
            throw new NotFoundException("Booking does not exist with id: " + id);
        }

        User user;
        try {
            user = restTemplate.getForObject(endPoint + "bookings/user/" + id, User.class);
        } catch (HttpClientErrorException exp) {
            throw new NotFoundException("No user found having booking id: " + id);
        }

        ByteArrayOutputStream outputStream = null;
        try {
            //construct the text body part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText("Hi " + user.getUserName() + ", Your booking is confirmed!");

            //now write the PDF content to the output stream
            outputStream = new ByteArrayOutputStream();
            generateInvoiceEmail(outputStream, booking);
            byte[] bytes = outputStream.toByteArray();

            //construct the pdf body part
            DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
            MimeBodyPart pdfBodyPart = new MimeBodyPart();
            pdfBodyPart.setDataHandler(new DataHandler(dataSource));
            pdfBodyPart.setFileName("movie_ticket.pdf");

            //construct the mime multi part
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(textBodyPart);
            mimeMultipart.addBodyPart(pdfBodyPart);

            //create the sender/recipient addresses
            InternetAddress isSender = new InternetAddress(sender);
            InternetAddress isRecipient = new InternetAddress(user.getUserEmail());

            //construct the mime message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            mimeMessage.setSender(isSender);
            mimeMessage.setSubject("Your Ticket!!");
            mimeMessage.setRecipient(Message.RecipientType.TO, isRecipient);
            mimeMessage.setContent(mimeMultipart);

            //send off the email
            javaMailSender.send(mimeMessage);
            return booking;
        } catch (AddressException e) {
            throw new IllegalArgumentException("Email address invalid");
        } catch (MessagingException e) {
            throw new NotFoundException("Email Service Unavailable");
        } catch (IOException ioException) {
            throw new IOException("Something went wrong");
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void PDFDesign(PdfPTable table, Booking booking) {
        PdfPCell cellTitle = new PdfPCell();
        cellTitle.setBackgroundColor(Color.decode("#db4242"));
        cellTitle.setPadding(7);
        cellTitle.setHorizontalAlignment(Phrase.ALIGN_CENTER);
        com.lowagie.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA);
        fontTitle.setColor(Color.WHITE);

        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Phrase.ALIGN_CENTER);
        cell.setBackgroundColor(Color.lightGray);
        cell.setPadding(7);
        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.BLACK);

        cellTitle.setPhrase(new Phrase("Movie Name", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase(booking.getMovieName(), font));
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Theatre Name", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase(booking.getTheatreName(), font));
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Slot Id", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase(String.valueOf(booking.getSlot().getSlotId()), font));
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Slot Time", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase(booking.getSlot().getSlotTime().toString(), font));
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Slot Date", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase(booking.getSlot().getSlotDate().toString(), font));
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Seat Nos.", fontTitle));
        table.addCell(cellTitle);
        List<Integer> seatNo = new ArrayList<>();
        for (Seat seat : booking.getSeatList()) {
            seatNo.add(seat.getSeatNumber());
        }
        cell.setPhrase(new Phrase(
                Arrays
                    .toString(seatNo.toArray())
                    .replace("[", "")
                    .replace("]", "")
                , font
            )
        );
        table.addCell(cell);

        cellTitle.setPhrase(new Phrase("Total Cost", fontTitle));
        table.addCell(cellTitle);
        cell.setPhrase(new Phrase("Rs. " + booking.getBookingAmount(), font));
        table.addCell(cell);
    }

    public void generateInvoiceEmail(OutputStream outputStream,
                                     Booking booking) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        writeIntoPdf(document, booking);
    }

    public String generateInvoice(int id,
                                  Principal principal,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
        throws IOException, NotFoundException, ConnectionException {
        log.debug("Generating invoice for booking id : " + id);

        ObjectMapper mapper = new ObjectMapper();

        Booking booking;
        try {
            booking = mapper.convertValue(CommunicationService.RequestMirror(
                null,
                HttpMethod.GET,
                request,
                "/bookings/" + id),
                Booking.class);
        } catch (HttpClientErrorException e) {
            throw new NotFoundException("No booking found with booking id: " + id);
        } catch (URISyntaxException e) {
            throw new ConnectionException("Unable to fetch booking");
        }
        if(booking.isCancelled()) {
            throw new IllegalArgumentException(
                "Can't generate invoice as booking with id: " + id + " already cancelled");
        }

        User user;
        try {
            user = mapper.convertValue(CommunicationService.RequestMirror(
                null,
                HttpMethod.GET,
                request,
                "/bookings/user/" + id),
                User.class);
        } catch (HttpClientErrorException e) {
            throw new NotFoundException("No user found with email: " + principal.getName());
        } catch (URISyntaxException e) {
            throw new ConnectionException("Unable to fetch user");
        }
        if (!user.getUserEmail().equals(principal.getName())
            && !principal.getName().equals("admin@bym.com")) {
            throw new IllegalArgumentException(
                "You can't generate invoice as booking id : " + id + " doesn't belong to you.");
        }

        response.setContentType("application/pdf");
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        writeIntoPdf(document, booking);
        return "invoice generated for booking id : " + booking.getBookingId();
    }

    public void writeIntoPdf(Document document, Booking booking) throws IOException {
        document.open();
        com.lowagie.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(Color.BLACK);

        // adding bookurmovie logo
        com.lowagie.text.Image image = com.lowagie.text.Image.getInstance("src/main/resources/bookurmovie.png");
        image.scaleAbsolute(300f, 300f);
        image.setAlignment(Image.ALIGN_CENTER);
        image.setAlt("BYM");
        document.add(image);


        // for heading at the top
        Paragraph p = new Paragraph("MOVIE TICKET", fontTitle);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        // movie details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80f);
        table.setWidths(new float[]{1f, 2f});
        table.setSpacingBefore(20);
        PDFDesign(table, booking);
        document.add(table);
        document.addTitle("Movie Ticket");
        document.close();
    }
}
