package ua.salon.schedule.singleton_thread_executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.salon.schedule.command.GetAllBookingsByDayCommand;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public enum ScheduledExecutor {
    INSTANCE;
    private ScheduledExecutorService executor;
    private GetAllBookingsByDayCommand command;
    private static final Logger rootLogger = LogManager.getRootLogger();

    ScheduledExecutor() {
        executor = Executors.newScheduledThreadPool(1);
        command = new GetAllBookingsByDayCommand();
    }

    public GetAllBookingsByDayCommand getCommand() {
        return command;
    }

    public static ScheduledExecutor getInstance() {
        return INSTANCE;
    }

    public void threadInvoke() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
        ZonedDateTime nextRun = now.withHour(TimeAttributes.SCHEDULED_HOUR.getValue()).withMinute(TimeAttributes.SCHEDULED_MINUTE.getValue()).withSecond(TimeAttributes.SCHEDULED_SECONDS.getValue());
        rootLogger.debug("Next run of executor" + nextRun);
        if(now.compareTo(nextRun) > 0){
            nextRun = nextRun.plusDays(1);
            rootLogger.debug("nextRun"+nextRun);
        }
        Duration duration = Duration.between(now, nextRun);
        rootLogger.debug("duration"+duration);
        long initalDelay = duration.getSeconds();
        rootLogger.debug("initalDelay"+initalDelay);
        try{
            Runnable task = () -> command.invokeDAO(LocalDate.now().toString());
            executor.scheduleAtFixedRate(task,initalDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        } catch (Exception e) {
            rootLogger.warn("Exception occured in threadInvoke()", e);
        }
    }

    public void threadShutdown() {
        executor.shutdownNow();
        rootLogger.debug("Executor of daily email sender successfully shutdown" + executor.isShutdown());
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            rootLogger.warn(ScheduledExecutor.class.getName(), e);
        }
    }
}
