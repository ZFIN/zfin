EXECUTE FUNCTION SYSBldPrepare('ifxmngr', 'sysblderrorlog'); 
EXECUTE FUNCTION SYSBldUnRegister('bts.3.00','sysblderrorlog');
EXECUTE FUNCTION SYSBldRegister('bts.3.00','sysblderrorlog');
