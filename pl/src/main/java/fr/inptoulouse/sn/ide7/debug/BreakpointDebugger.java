package fr.inptoulouse.sn.ide7.debug;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BreakpointDebugger {

    public void debug(String className, int breakpointLine) throws Exception {
        LaunchingConnector connector =
                Bootstrap.virtualMachineManager().defaultConnector();

        Map<String, Connector.Argument> arguments =
                connector.defaultArguments();

        arguments.get("main").setValue(className);
        arguments.get("options").setValue("-cp bin");
        arguments.get("suspend").setValue("true");

        VirtualMachine vm = connector.launch(arguments);

        EventRequestManager manager = vm.eventRequestManager();

        ClassPrepareRequest prepareRequest =
                manager.createClassPrepareRequest();

        prepareRequest.addClassFilter(className);
        prepareRequest.enable();

        vm.resume();

        while (true) {
            EventSet eventSet = vm.eventQueue().remove();

            for (com.sun.jdi.event.Event event : eventSet) {

                if (event instanceof ClassPrepareEvent) {
                    ClassPrepareEvent prepareEvent = (ClassPrepareEvent) event;
                    ReferenceType refType = prepareEvent.referenceType();

                    List<Location> locations =
                            refType.locationsOfLine(breakpointLine);

                    if (!locations.isEmpty()) {
                        BreakpointRequest breakpoint =
                                manager.createBreakpointRequest(locations.get(0));

                        breakpoint.enable();

                        System.out.println(
                                "Breakpoint placé à la ligne " + breakpointLine
                        );
                    }
                }

                if (event instanceof BreakpointEvent) {
                    BreakpointEvent breakpointEvent = (BreakpointEvent) event;
                    System.out.println(
                            "Breakpoint atteint à la ligne "
                                    + breakpointEvent.location().lineNumber()
                    );

                    System.out.println("Programme Java suspendu sur le breakpoint.");
                    System.out.println("Appuie sur Entrée pour reprendre l'exécution...");

                    new Scanner(System.in).nextLine();

                    System.out.println("Reprise de l'exécution.");

                    eventSet.resume();
                    return;
                }

                if (event instanceof VMDisconnectEvent
                        || event instanceof VMDeathEvent) {
                    System.out.println(
                            "Programme terminé sans atteindre le breakpoint."
                    );
                    return;
                }
            }

            eventSet.resume();
        }
    }
}
