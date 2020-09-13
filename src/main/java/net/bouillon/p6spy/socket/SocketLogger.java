package net.bouillon.p6spy.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.P6Logger;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;


public class SocketLogger implements P6Logger {

    private static final Logger log = LoggerFactory.getLogger(SocketLogger.class);

    final Server server;

    final Subject<SqlLog> sqlSubject = PublishSubject.create();
    final Flowable<SqlLog> sqlObservable = sqlSubject.toFlowable(BackpressureStrategy.BUFFER).observeOn(Schedulers.io());

//    final Subject<Exception> exceptionSubject = PublishSubject.create();
//    final Flowable<Exception> exceptionObservable = exceptionSubject.toFlowable(BackpressureStrategy.BUFFER).observeOn(Schedulers.io());

//    final Subject<String> textSubject = PublishSubject.create();
//    final Flowable<String> textObservable = textSubject.toFlowable(BackpressureStrategy.BUFFER).observeOn(Schedulers.io());

    public SocketLogger() {
        super();
        server = new Server(sqlObservable);
        server.startServer(4564);
    }

    @Override
    public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
        SqlLog sqlLog = new SqlLog(connectionId, now, elapsed, category.toString(), prepared, sql, url);
        sqlSubject.onNext(sqlLog);
    }

    @Override
    public void logException(Exception e) {
//        exceptionSubject.onNext(e);
    }

    @Override
    public void logText(String text) {
//        textSubject.onNext(text);
    }

    @Override
    public boolean isCategoryEnabled(Category category) {
        // no restrictions on logger side
        return true;
    }


    private static class Server {

        AsynchronousServerSocketChannel serverChannel;

        private final Flowable<SqlLog> sqlObservable;

        private Server(final Flowable<SqlLog> sqlObservable) {
            this.sqlObservable = sqlObservable;
        }

        public void startServer(int port) {
            try {
                log.info("Listening for P6Spy appender on port {}", port);
                serverChannel = AsynchronousServerSocketChannel.open();
                serverChannel.bind(new InetSocketAddress("127.0.0.1", port));

                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(final AsynchronousSocketChannel clientChannel, final Object attachment) {
                        if (serverChannel.isOpen()) {
                            serverChannel.accept(null, this);
                        }

                        if ((clientChannel != null) && (clientChannel.isOpen())) {
                            new ClientHandler(clientChannel, sqlObservable);
                        }
                    }

                    @Override
                    public void failed(final Throwable exc, final Object attachment) {

                    }
                });

                log.info("Done setup socket {}", serverChannel);
            } catch (IOException e) {
                log.info("Error during setup p6spy socket {}", serverChannel);
                log.error(e.getMessage(), e);
            }
        }

        public void stopServer() {
            try {
                if (serverChannel != null) {
                    serverChannel.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }


        }

    }


    private static class ClientHandler {

        private final AsynchronousSocketChannel clientChannel;

        private final Disposable subscribeSql;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final ObjectWriter objectWriter = objectMapper.writer();

        public ClientHandler(final AsynchronousSocketChannel clientChannel, Flowable<SqlLog> sqlObservable) {
            this.clientChannel = clientChannel;
            final OutputStream outputStream = Channels.newOutputStream(clientChannel);
            final PrintWriter outWriter = new PrintWriter(outputStream, true);
            final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            log.info("Client {} subscribing to p6spy event", clientChannel);
            subscribeSql = sqlObservable.subscribe(sql -> outWriter.println(objectWriter.writeValueAsString(sql)));

            // loop on read to close the connection when client disconnects
            clientChannel.read(readBuffer, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(final Integer bytesRead, final Object attachment) {
                    log.warn("Received {} bytes: {}", bytesRead, readBuffer);
                    // loop on read
                    if (bytesRead != -1) {
                        clientChannel.read(readBuffer, null, this);
                    } else {
                        close(outWriter);
                    }
                }

                @Override
                public void failed(final Throwable exc, final Object attachment) {
                    if (!(exc instanceof InterruptedByTimeoutException)) {
                        log.error(exc.getMessage(), exc);
                        close(outWriter);
                    } else {
                        // loop on read
                        clientChannel.read(readBuffer, null, this);
                    }
                }
            });


        }

        public void close(final PrintWriter outWriter) {
            try {
                log.info("Closing {}", clientChannel);
                if (subscribeSql != null) {
                    log.info("Client {} unsubscribing from p6spy event", clientChannel);
                    subscribeSql.dispose();
                }
                if (outWriter != null) {
                    outWriter.close();
                }
                clientChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
