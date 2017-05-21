//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

/**
 * This class is for the request and response constants that are used in the FIPAY service.
 * It is also used for the formatters to determine index number of where to put things in the 
 * message and parse information out of the response.
 * 
 * @author mlawrence
 * 
 */
public interface GDYNFIPAYRequestResponseConstantsIfc
{

    // Comma Delimited Constant
    public static final String COMMA = ",";

    // line length for returned receipt text
    public static final int RECEIPT_LINE = 41;

    // message field length for credit/debit/giftCard requests and responses
    public static final int CREDIT_DEBIT_FIELD_LENGTH = 21;
    public static final int REVERSAL_FIELD_LENGTH = 24;
    public static final int COMMON_RESPONSE_FIELD_LENGTH = 51;
    public static final int GIFT_CARD_RESPONSE_FIELD_LENGTH = 60;
    public static final int NON_ERROR_RETRY_RESPONSE_FIELD_LENGTH = 81;
    /*
     * REQUEST CONSTANTS
     */
    public static final String CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE = "100";
    public static final String CREDIT_DEBIT_AUTH_SAF_REQ_TRANSACTION_TYPE = "111";
    public static final String DEBITACKREQ_TRANSACTION_TYPE = "901";

    /*
     * RESPONSE CONSTANTS
     */
    // Message Transaction Type Response Codes
    public static final String CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE = "101";
    public static final String FORMATTED_CUSTOMER_RECEIPT_TRANSACTION_TYPE = "102";
    public static final String FORMATTED_END_OF_DAY_AUTH_SUMMARY_REP_TRAN_TYPE = "102";
    public static final String FORMATTED_STORE_RECEIPT_TRANSACTION_TYPE = "103";
    public static final String PIN_PAD_PROGRESS_MESSAGES_TRANSACTION_TYPE = "105";
    public static final String CREDIT_DEBIT_AUTH_SAF_RESP_TRANSACTION_TYPE = "112";
    public static final String SALES_AUDIT_RECORD_TRANSACTION_TYPE = "115";
    public static final String DEBITACKREQ_RESPONSE_TRANSACTION_TYPE = "902";

    // Action Response Codes
    public static final String CREDIT_DEBIT_APPROVED = "0";
    public static final String CREDIT_DEBIT_DECLINED = "1";
    public static final String CREDIT_DEBIT_CALL_REFERRAL = "2";
    public static final String CREDIT_DEBIT_BANK_DOWN = "3";
    public static final String CREDIT_DEBIT_FORMATTING_ISSUE = "6";
    public static final String CREDIT_DEBIT_TRY_AGAIN = "8";
    public static final String CREDIT_DEBIT_TIME_OUT = "10";
    public static final String CREDIT_DEBIT_ADMIN_APPROVED = "12";
    public static final String CREDIT_DEBIT_MAC_FAILURE = "14";
    public static final String CREDIT_DEBIT_ERROR = "20";

    // Store and Forward Eligible
    public static final String SAF_ABLE = "SAFable";

    // Field Array Indexes
    public static final int IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX = 0;

    public static final int IX_CREDIT_FIELD2_RESERVED_INDEX = 1;
    public static final int IX_DEBIT_INTERFACE_ID_INDEX = 1;
    public static final int FORMATTED_RECEIPT = 1;

    public static final int IX_CREDIT_INTERFACE_ID_INDEX = 2;
    public static final int IX_DEBIT_FIELD3_RESERVED_INDEX = 2;

    public static final int IX_CREDIT_DEBIT_ACTION_CODE_INDEX = 3;

    public static final int IX_CREDIT_DEBIT_TIME_OUT_INDEX = 4;

    public static final int IX_CREDIT_DEBIT_INDEX = 5;

    public static final int IX_CREDIT_FIELD7_RESERVED_INDEX = 6;
    public static final int IX_DEBIT_TERM_ID_INDEX = 6;

    public static final int IX_CREDIT_DEBIT_STORE_NUMBER_INDEX = 7;

    public static final int IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX = 8;

    public static final int IX_CREDIT_DEBIT_TRAN_TYPE_INDEX = 9;

    public static final int IX_CREDIT_FORCE_ONLINE_INDEX = 10;
    public static final int IX_DEBIT_FIELD11_RESERVED_INDEX = 10;

    public static final int IX_CREDIT_DEBIT_FIELD12_RESERVED_INDEX = 11;

    public static final int IX_CREDIT_DEBIT_ACCOUNT_INDEX = 12;

    public static final int IX_CREDIT_DEBIT_EXP_DATE_INDEX = 13;

    public static final int IX_CREDIT_DEBIT_SWIPE_INDEX = 14;

    public static final int IX_CREDIT_DEBIT_AMOUNT_INDEX = 15;

    public static final int IX_CREDIT_DEBIT_INVOICE_INDEX = 16;

    public static final int IX_CREDIT_DEBIT_TRAN_LANG_INDEX = 17;

    public static final int IX_CREDIT_DEBIT_FORCE_AUTH_CODE_INDEX = 18;

    public static final int IX_CREDIT_DEBIT_ORIG_AMOUNT_INDEX = 19;

    public static final int IX_CREDIT_DEBIT_OPTIONS_INDEX = 20;

    public static final int IX_DEBIT_PIN_PAD_INDEX = 21;
    public static final int IX_CREDIT_FIELD22_RESERVED_INDEX = 21;

    public static final int IX_DEBIT_PIN_PAD_ID_INDEX = 22;
    public static final int IX_CREDIT_FIELD23_RESERVED_INDEX = 22;

    public static final int IX_CREDIT_DEBIT_OPERATOR_INDEX = 23;

    public static final int IX_CREDIT_DEBIT_SUPER_SWIPE_INDEX = 24;

    public static final int IX_DEF_CREDIT_SUPER_SWIPE_INDEX = 25;
    public static final int IX_DEBIT_FIELD26_RESERVED_INDEX = 25;

    public static final int IX_DEF_DEBIT_SUPER_SWIPE_INDEX = 26;
    public static final int IX_CREDIT_FIELD27_RESERVED_INDEX = 26;

    public static final int IX_CREDIT_DEBIT_FIELD28_RESERVED_INDEX = 27;

    public static final int IX_CREDIT_DEBIT_CHECK_DRIVERS_LIC_INDEX = 28;

    public static final int IX_CREDIT_DEBIT_FIELD30_RESERVED_INDEX = 29;

    public static final int IX_CREDIT_MAIL_ORDER_AVS_INDEX = 30;
    public static final int IX_DEBIT_FIELD31_RESERVED_INDEX = 30;

    public static final int IX_CREDIT_DEBIT_STORE_ADDRESS_INDEX = 31;

    public static final int IX_CREDIT_DEBIT_STORE_ADDRESS1_INDEX = 32;

    public static final int IX_CREDIT_DEBIT_STORE_ADDRESS2_INDEX = 33;

    public static final int IX_CREDIT_DEBIT_STORE_ADDRESS3_INDEX = 34;

    public static final int IX_CREDIT_DEBIT_SPDH_CODE_INDEX = 35;

    public static final int IX_CREDIT_DEBIT_AUTH_CODE_INDEX = 36;

    public static final int IX_CREDIT_DEBIT_RECEIPT_DISPLAY_INDEX = 37;

    public static final int IX_CREDIT_DEBIT_CREDIT_MERCHANT_INDEX = 38;

    public static final int IX_CREDIT_DEBIT_FIELD40_RESERVED_INDEX = 39;

    public static final int IX_DEBIT_FIELD41_RESERVED_INDEX = 40;
    public static final int IX_CREDIT_DEFAULT_TIMEOUT_INDEX = 40;

    public static final int IX_DEBIT_FIELD42_RESERVED_INDEX = 41;
    public static final int IX_CREDIT_DEFAULT_DRAFT_CAPTURE_INDEX = 41;

    public static final int IX_CREDIT_DEBIT_FIELD43_RESERVED_INDEX = 42;

    public static final int IX_CREDIT_DEBIT_CREDIT_PS2000_INDEX = 43;

    public static final int IX_CREDIT_DEBIT_FIELD45_RESERVED_INDEX = 44;

    public static final int IX_CREDIT_DEBIT_CREDIT_SEQ_NUMBER_INDEX = 45;

    public static final int IX_CREDIT_DEBIT_CREDIT_BATCH_NUMBER_INDEX = 46;

    public static final int IX_CREDIT_DEBIT_FIELD48_RESERVED_INDEX = 47;

    public static final int IX_CREDIT_DEBIT_LANGUAGE_INDEX = 48;

    public static final int IX_CREDIT_DEBIT_DATE_INDEX = 49;

    public static final int IX_CREDIT_DEBIT_TIME_INDEX = 50;

    public static final int IX_CREDIT_DEBIT_DEPOSIT_DATA_INDEX = 51;

    public static final int IX_CREDIT_DEBIT_ISO_RESP_INDEX = 52;

    public static final int IX_CREDIT_DEBIT_FIELD54_RESERVED_INDEX = 53;

    public static final int IX_CREDIT_DEBIT_FIELD55_RESERVED_INDEX = 54;

    public static final int IX_CREDIT_DEBIT_SWIPE_FLAG_INDEX = 55;

    public static final int IX_CREDIT_FIELD57_RESERVED_INDEX = 56;
    public static final int IX_DEBIT_COM_LINK_INDEX = 56;

    public static final int IX_CREDIT_FIELD58_RESERVED_INDEX = 57;
    public static final int IX_DEBIT_COM_BAUD_RATE_INDEX = 57;

    public static final int IX_CREDIT_FIELD59_RESERVED_INDEX = 58;
    public static final int IX_DEBIT_ACCOUNT_INDEX = 58;

    public static final int IX_CREDIT_FIELD60_RESERVED_INDEX = 59;
    public static final int IX_DEBIT_PIN_PAD_DISPLAY_INDEX = 59;

    public static final int IX_CREDIT_DEBIT_ADDTL_MESSAGE_INDEX = 60;

    public static final int IX_CREDIT_DEBIT_FIELD62_RESERVED_INDEX = 61;
    public static final int IX_CREDIT_DEBIT_FIELD63_RESERVED_INDEX = 62;
    public static final int IX_CREDIT_DEBIT_FIELD64_RESERVED_INDEX = 63;
    public static final int IX_CREDIT_DEBIT_FIELD65_RESERVED_INDEX = 64;

    public static final int IX_CREDIT_DEBIT_HOST_RESP_TIME_INDEX = 65;

    public static final int IX_DEBIT_FIELD67_RESERVED_INDEX = 66;
    public static final int IX_CREDIT_TRACK1_INDEX = 66;

    public static final int IX_CREDIT_DEBIT_FIELD68_RESERVED_INDEX = 67;
    public static final int IX_CREDIT_DEBIT_FIELD69_RESERVED_INDEX = 68;
    public static final int IX_CREDIT_DEBIT_FIELD70_RESERVED_INDEX = 69;

    public static final int IX_DEBIT_FIELD71_RESERVED_INDEX = 70;
    public static final int IX_CREDIT_TERMINAL_ID_INDEX = 70;

    public static final int IX_CREDIT_DEBIT_TERMINAL_ID_INDEX = 71;
    public static final int IX_DEBIT_PRIME_DEBIT_TERM_ID = 71;

    public static final int IX_DEBIT_FIELD73_RESERVED_INDEX = 72;
    public static final int IX_CREDIT_SAF_TERMINAL_ID_INDEX = 72;

    public static final int IX_CREDIT_DEBIT_FIELD74_RESERVED_INDEX = 73;

    public static final int IX_CREDIT_DEBIT_BANK_DEP_DATA_INDEX = 74;

    public static final int IX_CREDIT_DEBIT_FIELD76_RESERVED_INDEX = 75;
    public static final int IX_CREDIT_DEBIT_FIELD77_RESERVED_INDEX = 76;
    public static final int IX_CREDIT_DEBIT_FIELD78_RESERVED_INDEX = 77;
    public static final int IX_CREDIT_DEBIT_FIELD79_RESERVED_INDEX = 78;
    public static final int IX_CREDIT_DEBIT_FIELD80_RESERVED_INDEX = 79;
    public static final int IX_CREDIT_DEBIT_FIELD81_RESERVED_INDEX = 80;
    public static final int IX_CREDIT_DEBIT_FIELD82_RESERVED_INDEX = 81;
    public static final int IX_CREDIT_DEBIT_FIELD83_RESERVED_INDEX = 82;
    public static final int IX_CREDIT_DEBIT_FIELD84_RESERVED_INDEX = 83;

    public static final int IX_CREDIT_FIELD85_RESERVED_INDEX = 84;
    public static final int IX_DEBIT_PIN_BLOCK_INDEX = 84;

    public static final int IX_CREDIT_DEBIT_FIELD86_RESERVED_INDEX = 85;
    public static final int IX_CREDIT_DEBIT_FIELD87_RESERVED_INDEX = 86;
    public static final int IX_CREDIT_DEBIT_FIELD88_RESERVED_INDEX = 87;

    public static final int IX_DEBIT_FIELD89_RESERVED_INDEX = 88;
    public static final int IX_CREDIT_IS_SAFABLE_INDEX = 88;

    public static final int IX_CREDIT_DEBIT_FIELD90_RESERVED_INDEX = 89;

    public static final int IX_CREDIT_DEF_DEBIT_SUPER_ID_INDEX = 90;
    public static final int IX_DEBIT_FIELD91_RESERVED_INDEX = 90;

    public static final int IX_CREDIT_DEF_CREDIT_SUPER_PWD_INDEX = 91;
    public static final int IX_DEBIT_FIELD92_RESERVED_INDEX = 91;

    public static final int IX_CREDIT_DEF_DEBIT_SUPER_PWD_INDEX = 92;
    public static final int IX_DEBIT_FIELD93_RESERVED_INDEX = 92;

    public static final int IX_CREDIT_SUPER_PIN_BLOCK_INDEX = 93;
    public static final int IX_DEBIT_FIELD94_RESERVED_INDEX = 93;

    public static final int IX_CREDIT_DEBIT_FIELD95_RESERVED_INDEX = 94;
    public static final int IX_CREDIT_DEBIT_FIELD96_RESERVED_INDEX = 95;
    public static final int IX_CREDIT_DEBIT_FIELD97_RESERVED_INDEX = 96;
    public static final int IX_CREDIT_DEBIT_FIELD98_RESERVED_INDEX = 97;
    public static final int IX_CREDIT_DEBIT_FIELD99_RESERVED_INDEX = 98;
    public static final int IX_CREDIT_DEBIT_FIELD100_RESERVED_INDEX = 99;
    public static final int IX_CREDIT_DEBIT_FIELD101_RESERVED_INDEX = 100;
    public static final int IX_CREDIT_DEBIT_FIELD102_RESERVED_INDEX = 101;
    public static final int IX_CREDIT_DEBIT_FIELD103_RESERVED_INDEX = 102;
    public static final int IX_CREDIT_DEBIT_FIELD104_RESERVED_INDEX = 103;
    public static final int IX_CREDIT_DEBIT_FIELD105_RESERVED_INDEX = 104;
    public static final int IX_CREDIT_DEBIT_FIELD106_RESERVED_INDEX = 105;
    public static final int IX_CREDIT_DEBIT_FIELD107_RESERVED_INDEX = 106;
    public static final int IX_CREDIT_DEBIT_FIELD108_RESERVED_INDEX = 107;
    public static final int IX_CREDIT_DEBIT_FIELD109_RESERVED_INDEX = 108;
    public static final int IX_CREDIT_DEBIT_FIELD110_RESERVED_INDEX = 109;

    public static final int IX_CREDIT_VEHICLE_INDEX = 110;
    public static final int IX_DEBIT_FIELD111_RESERVED_INDEX = 110;

    public static final int IX_CREDIT_ODOMETER_INDEX = 111;
    public static final int IX_DEBIT_FIELD112_RESERVED_INDEX = 111;

    public static final int IX_CREDIT_DRIVER_INDEX = 112;
    public static final int IX_DEBIT_FIELD113_RESERVED_INDEX = 112;

    public static final int IX_CREDIT_DEBIT_FIELD114_RESERVED_INDEX = 113;

    public static final int IX_CREDIT_EMV_TAGS_INDEX = 114;
    public static final int IX_DEBIT_FIELD115_RESERVED_INDEX = 114;

    public static final int IX_CREDIT_DEBIT_CLEAR_MKDN_AMT_INDEX = 115;

    public static final int IX_CREDIT_DEBIT_FIELD117_RESERVED_INDEX = 116;
    public static final int IX_CREDIT_DEBIT_FIELD118_RESERVED_INDEX = 117;
    public static final int IX_CREDIT_DEBIT_FIELD119_RESERVED_INDEX = 118;
    public static final int IX_CREDIT_DEBIT_FIELD120_RESERVED_INDEX = 119;
    public static final int IX_CREDIT_DEBIT_FIELD121_RESERVED_INDEX = 120;
    public static final int IX_CREDIT_DEBIT_FIELD122_RESERVED_INDEX = 121;
    public static final int IX_CREDIT_DEBIT_FIELD123_RESERVED_INDEX = 122;
    public static final int IX_CREDIT_DEBIT_FIELD124_RESERVED_INDEX = 123;
    public static final int IX_CREDIT_DEBIT_FIELD125_RESERVED_INDEX = 124;
    public static final int IX_CREDIT_DEBIT_FIELD126_RESERVED_INDEX = 125;
    public static final int IX_CREDIT_DEBIT_FIELD127_RESERVED_INDEX = 126;
    public static final int IX_CREDIT_DEBIT_FIELD128_RESERVED_INDEX = 127;
    public static final int IX_CREDIT_DEBIT_FIELD129_RESERVED_INDEX = 128;
    public static final int IX_CREDIT_DEBIT_FIELD130_RESERVED_INDEX = 129;
    public static final int IX_CREDIT_DEBIT_FIELD131_RESERVED_INDEX = 130;
    public static final int IX_CREDIT_DEBIT_FIELD132_RESERVED_INDEX = 131;
    public static final int IX_CREDIT_DEBIT_FIELD133_RESERVED_INDEX = 132;
    public static final int IX_CREDIT_DEBIT_FIELD134_RESERVED_INDEX = 133;
    public static final int IX_CREDIT_DEBIT_FIELD135_RESERVED_INDEX = 134;
    public static final int IX_CREDIT_DEBIT_FIELD136_RESERVED_INDEX = 135;
    public static final int IX_CREDIT_DEBIT_FIELD137_RESERVED_INDEX = 136;
    public static final int IX_CREDIT_DEBIT_FIELD138_RESERVED_INDEX = 137;

    /*
     * TRANSACTION TYPES
     */
    public static final String SALE = "Sale";
    public static final String REFUND = "Refund";
    public static final String VOID = "Void";
    public static final String INIT_DEBIT = "InitDebit";
    public static final String EOD = "Eod";

    /*
     * TENDER TYPES
     */
    public static final String CREDIT = "CREDIT";
    public static final String DEBIT = "DEBIT";
    public static final String GIFT_CARD = "GIFTCARD";

    // GIFT CARD REQUEST SUB TYPES
    // (full list of AJB GiftCard transaction types;
    // many are not used)

    public static final String GC_ACTIVATE = "Activate";
    // ActivateVirtualCard (Internet only)
    // BalanceAdjustment
    public static final String GC_BALANCE_INQUIRY = "BalanceInquiry";
    // BalanceMerge (Internet-only currently available)
    // BalanceTransfer
    // Cancel
    // CardInquiry
    // CashBack
    public static final String GC_CASH_OUT = "CashOut";
    // CloseAccount
    // Deactivate
    // EnablePhysicalCard (Internet only)
    public static final String GC_EOD = "EOD";
    // EODTrial
    // FundandActivate
    // FundAndActivateCommit
    // FundAndActivateInitiate
    // FundCommit
    // FundInitiate
    // FundOnly
    // InitDebit
    public static final String GC_ISSUE = "Issue";
    // MerchantReversal
    // Preauth
    // PreAuthComp
    public static final String GC_REDEEM = "Redeem";
    public static final String GC_REFUND = "Refund";
    public static final String GC_RELOAD = "Reload";
    // ReloadWithoutCard
    // RemoveVoidLock
    // Replacement
    public static final String GC_SALE = "Sale";
    // TransactionHistory
    public static final String GC_VOID_ACTIVATE = "VoidActivate";
    // VoidCashBack
    // VoidCashOut
    // VoidCloseAccount
    // VoidFund
    public static final String GC_VOID_ISSUE = "VoidIssue";
    public static final String GC_VOID_REDEEM = "VoidRedeem";
    public static final String GC_VOID_SALE = "VoidSale";
    public static final String GC_VOID_RELOAD = "VoidReload";
    // VoidRefund
    // VoidTrial
}
