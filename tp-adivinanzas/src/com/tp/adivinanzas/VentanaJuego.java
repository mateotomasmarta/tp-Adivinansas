package com.tp.adivinanzas;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Ventana unica de la app: un relato del juego (bitacora) arriba y, abajo, la
 * unica accion posible en cada momento (botones o campo de texto). No hay
 * popups: todo pasa dentro de esta misma ventana.
 *
 * El juego se ejecuta en un hilo aparte (ver MainSwing); esta clase le presta
 * a ese hilo metodos que BLOQUEAN hasta que la persona clickea algo en la UI
 * -equivalente a lo que hacia Scanner.nextLine() en la consola, pero resuelto
 * con una cola de un solo lugar en vez de I/O bloqueante-. Los componentes de
 * Swing solo se tocan dentro de SwingUtilities.invokeLater, como corresponde.
 */
public class VentanaJuego extends JFrame {
    private final JTextArea bitacora = new JTextArea();
    private final JPanel panelAccion = new JPanel();

    public VentanaJuego() {
        super("Adivina Quien");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 580);
        setLocationRelativeTo(null);

        bitacora.setEditable(false);
        bitacora.setLineWrap(true);
        bitacora.setWrapStyleWord(true);
        bitacora.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        bitacora.setMargin(new Insets(8, 8, 8, 8));

        panelAccion.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

        setLayout(new BorderLayout());
        add(new JScrollPane(bitacora), BorderLayout.CENTER);
        add(panelAccion, BorderLayout.SOUTH);
    }

    public void log(String texto) {
        SwingUtilities.invokeLater(() -> {
            bitacora.append(texto + "\n");
            bitacora.setCaretPosition(bitacora.getDocument().getLength());
        });
    }

    public void limpiarAccion() {
        SwingUtilities.invokeLater(() -> {
            panelAccion.removeAll();
            panelAccion.revalidate();
            panelAccion.repaint();
        });
    }

    public String pedirTexto(String etiqueta, String valorPorDefecto) {
        return pedirEnPantalla(resolver -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(etiqueta));
            JTextField campo = new JTextField(valorPorDefecto, 18);
            JButton boton = new JButton("Continuar");
            Runnable enviar = () -> resolver.accept(campo.getText());
            boton.addActionListener(e -> enviar.run());
            campo.addActionListener(e -> enviar.run());
            panel.add(campo);
            panel.add(boton);
            SwingUtilities.invokeLater(() -> {
                campo.requestFocusInWindow();
                campo.selectAll();
            });
            return panel;
        });
    }

    /**
     * Una fila de botones, uno por opcion: clickear uno resuelve la espera
     * con esa opcion. Sirve tanto para elegir un filtro como un personaje o
     * una opcion de menu, pasandole como se etiqueta cada elemento.
     */
    public <T> T pedirEleccion(List<T> opciones, Function<T, String> etiqueta) {
        return pedirEleccion(opciones, etiqueta, null);
    }

    /**
     * Igual que pedirEleccion, pero si botonVolver no es null agrega un
     * boton aparte que resuelve la espera con null en vez de con una opcion
     * de la lista: sirve para poder arrepentirse sin forzar una eleccion.
     */
    public <T> T pedirEleccion(List<T> opciones, Function<T, String> etiqueta, String botonVolver) {
        return pedirEnPantalla(resolver -> {
            JPanel lista = new JPanel();
            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            for (T opcion : opciones) {
                JButton boton = new JButton(etiqueta.apply(opcion));
                boton.setAlignmentX(Component.LEFT_ALIGNMENT);
                boton.addActionListener(e -> resolver.accept(opcion));
                lista.add(boton);
            }
            JScrollPane scroll = new JScrollPane(lista);
            scroll.setPreferredSize(new Dimension(700, Math.min(260, 34 * opciones.size() + 10)));

            if (botonVolver == null) {
                return scroll;
            }

            JPanel contenedor = new JPanel(new BorderLayout(0, 6));
            contenedor.add(scroll, BorderLayout.CENTER);
            JButton volver = new JButton(botonVolver);
            volver.addActionListener(e -> resolver.accept(null));
            JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pie.add(volver);
            contenedor.add(pie, BorderLayout.SOUTH);
            return contenedor;
        });
    }

    public void esperarContinuar(String etiquetaBoton) {
        pedirEnPantalla(resolver -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton boton = new JButton(etiquetaBoton);
            boton.addActionListener(e -> resolver.accept(Boolean.TRUE));
            panel.add(boton);
            return panel;
        });
    }

    /** ArrayBlockingQueue no admite null; este centinela representa "Atras". */
    private static final Object VOLVER = new Object();

    /**
     * El constructor recibe un "resolver": llamarlo no solo entrega la
     * respuesta al hilo del juego (que esta bloqueado en cola.take()), sino
     * que ademas vacia panelAccion en el momento del click, en el mismo hilo
     * EDT. Sin esto, los botones viejos quedan clickeables durante la breve
     * ventana hasta que el hilo del juego arma la pantalla siguiente.
     */
    @SuppressWarnings("unchecked")
    private <T> T pedirEnPantalla(Function<Consumer<T>, JComponent> constructor) {
        BlockingQueue<Object> cola = new ArrayBlockingQueue<>(1);
        Consumer<T> resolver = valor -> {
            if (cola.offer(valor == null ? VOLVER : valor)) {
                panelAccion.removeAll();
                panelAccion.revalidate();
                panelAccion.repaint();
            }
        };
        SwingUtilities.invokeLater(() -> {
            JComponent contenido = constructor.apply(resolver);
            panelAccion.removeAll();
            panelAccion.add(contenido);
            panelAccion.revalidate();
            panelAccion.repaint();
        });
        try {
            Object resultado = cola.take();
            return resultado == VOLVER ? null : (T) resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido esperando la interfaz.", e);
        }
    }
}
