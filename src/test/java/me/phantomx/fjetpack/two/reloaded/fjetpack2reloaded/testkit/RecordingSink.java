package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.testkit;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link MessageSink} that keeps what was sent instead of printing it.
 * <p>
 * The console is a {@link CommandSender} like any other, so one recording sender serves both halves -
 * and it is a {@link Proxy} because {@code CommandSender} has a wide surface while the code under
 * test only ever calls {@code sendMessage}.
 */
public final class RecordingSink implements MessageSink {

    /** Everything sent, console included, in order. */
    public final List<String> sent = new ArrayList<>();

    @Override
    public void send(CommandSender target, String message, Object... args) {
        if (message == null || message.isEmpty()) return;
        sent.add(args.length == 0 ? message : String.format(message, args));
    }

    @Override
    public void usePrefix(String prefix) {
        // the tests assert on the text, not on the prefix decoration
    }

    /** A sender that records into {@link #sent}, standing in for the console. */
    public CommandSender console() {
        return (CommandSender) Proxy.newProxyInstance(
                RecordingSink.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> {
                        sent.add(String.valueOf(args[0]));
                        yield null;
                    }
                    case "getName" -> "console";
                    case "toString" -> "CommandSender(console)";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

    /** Does any recorded line contain the fragment? */
    public boolean saw(String fragment) {
        return sent.stream().anyMatch(line -> line.contains(fragment));
    }

}
