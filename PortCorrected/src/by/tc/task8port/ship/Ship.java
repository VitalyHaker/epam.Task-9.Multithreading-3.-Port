package by.tc.task8port.ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import by.tc.task8port.port.Berth;
import by.tc.task8port.port.Port;
import by.tc.task8port.port.PortException;
import by.tc.task8port.warehouse.Container;
import by.tc.task8port.warehouse.Warehouse;

public class Ship implements Runnable {

    private final static Logger logger = Logger.getRootLogger();
    private volatile boolean stopThread = false;

    private String name;
    private Port port;
    private Warehouse shipWarehouse;

    public Ship(String name, Port port, int shipWarehouseSize) {
        this.name = name;
        this.port = port;
        shipWarehouse = new Warehouse(shipWarehouseSize);

    }

    public Warehouse getShipWarehouse() {
        return shipWarehouse;
    }

    public void setContainersToWarehouse(List<Container> containerList) {
        shipWarehouse.addContainer(containerList);
    }

    public String getName() {
        return name;
    }

    public void stopThread() {
        stopThread = true;
    }

    public void run() {
        try {
            while (!stopThread) {
                atSea();
                inPort();
                logger.debug("Корабль "+getName() + " имеет контейнеров на борту - "
                        + getShipWarehouse().getRealSize());
            }
        } catch (InterruptedException e) {
            logger.error("С кораблем случилась неприятность и он уничтожен.", e);
        } catch (PortException e) {
            logger.error("Порт закрыт из-за угрозы терракта!", e); // изменен текст ссобщения
        }
    }

    private void atSea() throws InterruptedException {
        Thread.sleep(1000);
    }


    private void inPort() throws PortException, InterruptedException {
    	// отсутствует семафор на количество кораблей в порту
        boolean isLockedBerth = false;
        Berth berth = null;
        try {
            isLockedBerth = port.lockBerth(this);

            if (isLockedBerth) {
                berth = port.getBerth(this);
                logger.debug("Корабль " + name + " пришвартовался к причалу " + berth.getId());
                ShipAction action = getNextAction();
                executeAction(action, berth);
            } else {
                logger.debug("Кораблю " + name + " отказано в швартовке к причалу ");
            }
        } finally {
            if (isLockedBerth) {
                port.unlockBerth(this);
                logger.debug("Корабль " + name + " отошел от причала " + berth.getId());
            }
        }

    }

    private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
        switch (action) {
            case LOAD_TO_PORT:
                loadToPort(berth);
                break;
            case LOAD_FROM_PORT:
                loadFromPort(berth);
                break;
        }
    }

    private boolean loadToPort(Berth berth) throws InterruptedException {

        int containersNumberToMove = conteinersToUnload();
        boolean result = false;

        logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
                + " контейнеров на склад порта.");

        result = berth.add(shipWarehouse, containersNumberToMove);

        if (!result) {
            logger.debug("Недостаточно места на складе порта для выгрузки кораблем "
                    + name + " " + containersNumberToMove + " контейнеров.");
        } else {
            logger.debug("Корабль " + name + " выгрузил " + containersNumberToMove
                    + " контейнеров в порт.");

        }
        return result;
    }

    private boolean loadFromPort(Berth berth) throws InterruptedException {

        int containersNumberToMove = conteinersToDownload();

        boolean result = false;

        logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
                + " контейнеров со склада порта.");

        result = berth.get(shipWarehouse, containersNumberToMove);

        if (result) {
            logger.debug("Корабль " + name + " загрузил " + containersNumberToMove
                    + " контейнеров из порта.");
        } else {
            logger.debug("Недостаточно места на корабле " + name + " для погрузки "
                    + containersNumberToMove + " контейнеров из порта");
        }

        return result;
    }
     private int conteinersToUnload() { //нельзя выгрузить больше, чем имеется на корабле
        Random random = new Random();
        return random.nextInt(getShipWarehouse().getRealSize()) + 1;
    }

    private int conteinersToDownload() {
        Random random = new Random();
        return random.nextInt(20) + 1;
    }

    private ShipAction getNextAction() {
        Random random = new Random();
        int value = random.nextInt(2000);
        if (getShipWarehouse().getRealSize() == 0) {
            return ShipAction.LOAD_FROM_PORT;
        } else if (value >= 1000) {
            return ShipAction.LOAD_TO_PORT;
        } else
            return ShipAction.LOAD_FROM_PORT;
    }

    @Override // переопределен метод hashCode
    public int hashCode() {
        int result = (stopThread ? 1 : 0);
        result = 16 * result + (getName() != null ? getName().hashCode() : 0);
        result = 16 * result + (port != null ? port.hashCode() : 0);
        result = 16 * result + (getShipWarehouse() != null ? getShipWarehouse().hashCode() : 0);
        return result;
    }
    
    @Override // переопределен метод equals
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ship ship = (Ship) o;

        if (stopThread != ship.stopThread) return false;
        if (getName() != null ? !getName().equals(ship.getName()) : ship.getName() != null) return false;
        if (port != null ? !port.equals(ship.port) : ship.port != null) return false;
        return getShipWarehouse() != null ? getShipWarehouse().equals(ship.getShipWarehouse()) : ship.getShipWarehouse() == null;

    }

    enum ShipAction {
        LOAD_TO_PORT, LOAD_FROM_PORT
    }
}