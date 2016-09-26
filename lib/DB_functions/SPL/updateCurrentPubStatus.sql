create procedure updateCurrentPubStatus ()

update pub_tracking_history
 set pth_status_is_current = 'f' ;

end procedure;
