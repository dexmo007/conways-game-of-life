package de.ostfalia.umwinf.ws16.conf;

import de.ostfalia.umwinf.ws16.logic.GameOfLife;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * model class for the field configuration
 */
@XmlRootElement
public class Config {
    /**
     * number of rows and columns
     */
    private int y;
    private int x;
    /**
     * coordinates of alive cells
     */
    private List<Point> alive = new LinkedList<>();

    public Config() {
    }

    public Config(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Config(GameOfLife gameOfLife) {
        this(gameOfLife.getColumnCount(), gameOfLife.getRowCount());
        boolean[][] field = gameOfLife.getField();
        for (int i = 0; i < field.length; i++) {
            boolean[] row = field[i];
            for (int j = 0; j < row.length; j++)
                if (row[j])
                    addPoint(j, i);
        }
    }

    public void addPoint(int x, int y) {
        alive.add(new Point(x, y));
    }

    @XmlAttribute
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @XmlAttribute
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @XmlElementWrapper
    @XmlElement(name = "point")
    public List<Point> getAlive() {
        return alive;
    }

    public void setAlive(List<Point> alive) {
        this.alive = alive;
    }

    @XmlRootElement
    public static class Point {
        private int x;
        private int y;

        public Point() {
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @XmlAttribute
        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        @XmlAttribute
        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;

    static {
        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            // won't happen
        }
    }

    public void save(File file) throws JAXBException {
        marshaller.marshal(this, file);
    }

    public static Config load(File config) throws JAXBException {
        return (Config) unmarshaller.unmarshal(config);
    }
}
