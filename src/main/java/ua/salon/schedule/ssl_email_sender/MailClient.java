package ua.salon.schedule.ssl_email_sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.salon.schedule.net.IPDefiner;
import ua.salon.schedule.command.GetAllBookingsByDayCommand;
import ua.salon.schedule.model.booking.Booking;
import ua.salon.schedule.singleton_executor.ScheduledExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

///**@Deprecated(since="9") see:https://docs.oracle.com/javase/9/docs/api/java/util/Observer.html*/
public enum  MailClient implements Observer {
    INSTANCE;
    private Observable observable;
    private static final Logger rootLogger = LogManager.getRootLogger();

    MailClient() {
        observable = ScheduledExecutor.INSTANCE.getCommand();
        observable.addObserver(this::update);
    }

    public static MailClient getInstance() {
        return INSTANCE;
    }

    private static ua.salon.schedule.ssl_email_sender.Sender sslSender = new ua.salon.schedule.ssl_email_sender.Sender(EmailAttributes.USERNAME.getValue(), EmailAttributes.PASSWORD.getValue());

    @Override
    public void update(Observable obs, Object arg) {
        rootLogger.debug("update() method started execution: "+this.getClass());
        if(obs instanceof GetAllBookingsByDayCommand) {
            rootLogger.debug("Observer was notified by GetAllBookingsByDayCommand.class extends Observable");
            List<Booking> result = ((ArrayList<Booking>) arg);
            if (result.size() > 0) {
                for (Booking booking : result) {
                    sslSender.send(EmailAttributes.SUBJECT.getValue(), EmailAttributes.TEXT.getValue() + EmailAttributes.LINK.getValue(),EmailAttributes.FROM_EMAIL.getValue(), booking.getClient().getEmail());
                }
            }
        }
    }
}
