package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.TookTooLongException;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;

import java.util.concurrent.*;

public class MapCreatorSubButton extends MySubButton {
    public MapCreatorSubButton(ButtonType button, MyButton parent) {
        super(button, parent);
    }

    protected void printException(Exception e) {
        String errorMessage;
        if (e instanceof TookTooLongException) errorMessage = "An infinite loop occurred! Please try again.";
        else errorMessage = e.getMessage();

        printErrorMessage(e, errorMessage);
    }


    /**
     * This method watches over a thread and interrupts it if it takes too long.
     * In essence, it prevents infinite loops.
     *
     * @param thread The thread to watch over
     */
    @Deprecated
    protected void watchOverThread(Thread thread) {
        //DO NOT QUESTION THIS SECTION
        //IT WAS NECESSARY TO ENSURE THAT THERE IS NO INFINITE LOOP

        Thread watchForInfiniteLoop = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            thread.interrupt();
            throw new IllegalArgumentException("Took too long lol");
        });

        try {
            watchForInfiniteLoop.start();
            thread.start();
            watchForInfiniteLoop.interrupt();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Took too long lol")) printException(new TookTooLongException("Took too long lol"));
            else printException(ex);
        }
    }

    /**
     * Utility method to run a given Callable task with a specified timeout.
     * If the task does not complete within the specified timeout, it will be aborted.
     *
     * @param <T>     The type of the result returned by the Callable task.
     * @param callable The task to be executed.
     * @param timeout  The maximum time to wait for the task to complete.
     * @param unit     The time unit of the timeout argument.
     * @return The result of the Callable task if it completes within the timeout.
     * @throws TimeoutException If the task does not complete within the specified timeout.
     * @throws Exception        If the task execution throws an exception.
     */
    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit unit) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(callable);

        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true); // Cancel the task
            throw e; // Propagate the exception
        } catch (ExecutionException e) {
            throw new Exception(e.getCause()); // Propagate the exception
        } finally {
            executor.shutdownNow(); // Shut down the executor
        }
    }
}
