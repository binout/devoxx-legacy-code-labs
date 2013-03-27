package legacy.hedge;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import legacy.error.CheckResult;
import legacy.error.ErrorLevel;
import legacy.persistence.StorageActionEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class HedgingPositionManagementImplTest {

    HedgingPositionManagementImpl mgmt;

    @Mock
    HedgingPositionMgt headingPositionMgt;

    @Before
    public void before()  {
        MockitoAnnotations.initMocks(this);
        mgmt = new HedgingPositionManagementImpl();
        mgmt.headingPositionMgt = new MockHeadingPositionMgt();
        mgmt.trading = new MockTradingDataAccessServiceImpl();
    }

    @Test(expected = NullPointerException.class)
    public void initAndSendHedgingPosition_should_return_npe_with_null_args() {
        mgmt.initAndSendHedgingPosition(null);
    }

    @Test
    public void initAndSendHedgingPosition_should_return_true() {
        HedgingPosition hp = new HedgingPosition();

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);

        assertThat(checkResult).isNotNull();
        assertThat(checkResult.isCheckIsOk()).isTrue();
        assertThat(checkResult.getResult()).isNotNull();
    }

    @Test
    public void initAndSendHedgingPosition_should_return_true_with_EXT() {
        HedgingPosition hp = new HedgingPosition();
        hp.setType(HedgingPositionTypeConst.EXT);

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);
        assertCommon(checkResult);

        HedgingPosition hpResult = checkResult.getResult();

        assertThat(hpResult.getBasprx()).isEqualTo(0);
        assertThat(hpResult.getCodetyptkt()).isEqualTo(42);
        assertThat(hpResult.getCodtyptra()).isEqualTo(new BigInteger("42"));
        assertThat(hpResult.getStorageUpdate()).isEqualTo(StorageActionEnum.UPDATE);
        assertThat(hpResult.getTransactionWay()).isNull();
        assertThat(hpResult.getType()).isEqualTo(HedgingPositionTypeConst.EXT);
        assertThat(hpResult.getCreDate()).isEqualTo("3910-10-09T00:00:00");
        assertThat(hpResult.getCombck()).isEqualTo("0 0 CONTROL: [0x0x0x01h]");
        assertThat(hpResult.getQuantity()).isEqualTo("0.0");
        assertThat(hpResult.getHedgingTransactionId()).isNull();

    }

    @Test
    public void initAndSendHedgingPosition_should_return_true_with_INI() {
        HedgingPosition hp = new HedgingPosition();
        hp.setType(HedgingPositionTypeConst.INI);

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);

        assertCommon(checkResult);

        HedgingPosition hpResult = checkResult.getResult();
        assertThat(hpResult.getBasprx()).isEqualTo(0);
        assertThat(hpResult.getCodetyptkt()).isEqualTo(20);
        assertThat(hpResult.getCodtyptra()).isEqualTo(new BigInteger("23"));
        assertThat(hpResult.getStorageUpdate()).isEqualTo(StorageActionEnum.CREATE);
        assertThat(hpResult.getTransactionWay()).isEqualTo("S");
        assertThat(hpResult.getType()).isEqualTo(HedgingPositionTypeConst.INI);
        assertThat(hpResult.getCreDate()).isNotNull();
        assertThat(hpResult.getCombck()).isEqualTo("0 0 CONTROL: [0x0x0x01h]");
        assertThat(hpResult.getQuantity()).isEqualTo("0.0");
        assertThat(hpResult.getHedgingTransactionId()).isNull();

    }

    private void assertCommon(CheckResult<HedgingPosition> checkResult) {
        assertThat(checkResult).isNotNull();
        assertThat(checkResult.isCheckIsOk()).isTrue();
        HedgingPosition hpResult = checkResult.getResult();
        assertThat(hpResult).isNotNull();

        assertThat(hpResult.getDaprx()).isNull();
        assertThat(hpResult.getDatefinthe()).isNull();
        assertThat(hpResult.getErrorLevel()).isNull();
        assertThat(hpResult.getHedgeMsg()).isNull();
        assertThat(hpResult.getIkRtH()).isEqualTo("autobot");
        assertThat(hpResult.getMsgdev()).isNull();
        assertThat(hpResult.getMsgerr()).isNull();
        assertThat(hpResult.getMsgusr()).isNull();
        assertThat(hpResult.getNiverr()).isNull();
        assertThat(hpResult.getNoticePeriodEndDate()).isNull();
        assertThat(hpResult.getPrxref()).isEqualTo(Double.parseDouble("0.0"));
        assertThat(hpResult.getStatus()).isEqualTo(HedgingPositionStatusConst.HEDGED);
        assertThat(hpResult.getTransactionId()).isEqualTo(0);
        assertThat(hpResult.getValueDate()).isNull();
        assertThat(hpResult.getId()).isEqualTo(0);
        assertThat(hpResult.getLastModification()).isNull();
    }

    @Test
    public void initAndSendHedgingPosition_should_return_false_with_CANCEL_TR() {
        HedgingPosition hp = new HedgingPosition();
        hp.setType(HedgingPositionTypeConst.CANCEL_TRANSACTION);

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);

        assertCommon(checkResult);

        HedgingPosition hpResult = checkResult.getResult();
        assertThat(hpResult.getBasprx()).isEqualTo(100);
        assertThat(hpResult.getCodetyptkt()).isEqualTo(20);
        assertThat(hpResult.getCodtyptra()).isEqualTo(new BigInteger("42"));
        assertThat(hpResult.getStorageUpdate()).isEqualTo(StorageActionEnum.UPDATE);
        assertThat(hpResult.getTransactionWay()).isNull();
        assertThat(hpResult.getType()).isEqualTo(HedgingPositionTypeConst.CANCEL_TRANSACTION);
        assertThat(hpResult.getCreDate()).isNotNull();
        assertThat(hpResult.getCombck()).isNull();
        assertThat(hpResult.getQuantity()).isNull();
        assertThat(hpResult.getHedgingTransactionId()).isNull();


    }

    @Test
    public void initAndSendHedgingPosition_should_return_false_with_CANCEL_PO() {
        HedgingPosition hp = new HedgingPosition();
        hp.setType(HedgingPositionTypeConst.CANCEL_POSITION);

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);

        assertCommon(checkResult);

        HedgingPosition hpResult = checkResult.getResult();

        assertThat(hpResult.getBasprx()).isEqualTo(100);
        assertThat(hpResult.getCodetyptkt()).isEqualTo(20);
        assertThat(hpResult.getCodtyptra()).isEqualTo(new BigInteger("42"));
        assertThat(hpResult.getStorageUpdate()).isEqualTo(StorageActionEnum.UPDATE);
        assertThat(hpResult.getTransactionWay()).isNull();
        assertThat(hpResult.getType()).isEqualTo(HedgingPositionTypeConst.CANCEL_POSITION);
        assertThat(hpResult.getCreDate()).isNotNull();
        assertThat(hpResult.getCombck()).isNull();
        assertThat(hpResult.getQuantity()).isNull();
        assertThat(hpResult.getHedgingTransactionId()).isEqualTo("0xid.42");

    }

    Object[] give_status_and_error_level() {
        return JUnitParamsRunner.$(
                JUnitParamsRunner.$(ErrorLevel.CONNECT_ERROR, HedgingPositionStatusConst.REJECTED),
                JUnitParamsRunner.$(ErrorLevel.FATAL_ERROR, null),
                JUnitParamsRunner.$(ErrorLevel.FUNCTIONAL_ERROR, HedgingPositionStatusConst.REJECTED),
                JUnitParamsRunner.$(ErrorLevel.BOOKING_MALFUNCTION, null)

        );
    }

    @Test
    @Parameters(method = "give_status_and_error_level")
    public void checkStatus_with_error_level(ErrorLevel errorLevel,  HedgingPositionStatusConst status) {
        mgmt.headingPositionMgt = headingPositionMgt;

        HedgingPosition hp = new HedgingPosition();
        hp.setId(0);
        hp.setErrorLevel(errorLevel);
        CheckResult<HedgingPosition> result = new CheckResult<>();
        result.setResult(hp);
        result.setCheckIsOk(false);
        Mockito.when(headingPositionMgt.hedgingPositionMgt(hp)).thenReturn(result);

        CheckResult<HedgingPosition> checkResult = mgmt.initAndSendHedgingPosition(hp);

        HedgingPosition hpResult = checkResult.getResult();
        assertThat(hpResult.getStatus()).isEqualTo(status);
    }

}
