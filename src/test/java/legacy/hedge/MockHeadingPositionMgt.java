package legacy.hedge;

public class MockHeadingPositionMgt extends HedgingPositionMgt {

    protected void sendHp(HedgingPosition hp) {
        System.out.println("Send Hp");
    }
}
