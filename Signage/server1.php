<?php
require_once __DIR__ . '/vendor/autoload.php';
use Workerman\Worker;
use Workerman\Lib\Timer;

// Create a Websocket server
$ws_worker = new Worker("websocket://0.0.0.0:2346");

// 4 processes
$ws_worker->count = 1;

$users = [];

$xml = simplexml_load_file('fdb.xml');
$found = false;
$command_picture = json_encode(array ('id'=>"capture",'val'=>0));

define('UPLOAD_DIR', '/tmp/images/');
define('error_scrn', '/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCAJmAwADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD2yiiiv89z/RwKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKA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ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFAH//2Q==');

$ws_worker->onWorkerStart = function($ws_worker) use (&$users, &$command_picture, &$xml)
{
    // 2.5 seconds
    $time_interval = 30; 
    $timer_id = Timer::add($time_interval, 
        function()
        {
            echo "Timer run\n";
			global $xml, $users, $command_picture;
			foreach ($xml->displays->display as $search) 
			{
				if($search->note == "Developer unit")
				{
					$send_address = (string) $search->address;
					if (array_key_exists($send_address, $users)) {
						$connection_from_server_to_fids = $users[$send_address];
						$connection_from_server_to_fids->send($command_picture);
					}
				}
			}
			$dir    = '/tmp/images/';
			$files1 = scandir($dir);
			unset ($files1[0]);
			unset ($files1[1]);
			foreach ($files1 as &$value) {
				$filename = $dir.$value;
				if (file_exists($filename)) {
					$time_r = filemtime($filename);
					$cur_time = time();
					if ($cur_time - $time_r > 40) {
						//unlink($filename);
						$error_scrn_img = base64_decode(error_scrn);
						//$file = UPLOAD_DIR . $filename . '.jpeg';
						$success = file_put_contents($filename, $error_scrn_img);
						print $success ? $filename : 'Unable to save the file.';
					}
				}
			}
			unset($value);
        }
    );
};

// Emitted when new connection come
$ws_worker->onConnect = function($connection) use (&$users)
{
	echo "New connection\n";
    $connection->onWebSocketConnect = function($connection) use (&$users)
    {
        // put get-parameter into $users collection when a new user is connected
        // you can set any parameter on site page. for example client.html: ws = new WebSocket("ws://127.0.0.1:8000/?user=tester01");
		global $xml;
		echo $connection->getRemoteIp();
        $user = (string) $_GET['user'];
		echo $user;
		$users[$user] = $connection;
		foreach ($xml->displays->display as $item) 
		{
			if($item->address == $user)
			{
				$found = (string) $item->url;
				break;
			}
		}
		$command_url = json_encode(array ('id'=>'desk_src','val'=>$found));
		$connection_to_fids = $users[$user];
		$connection_to_fids->send($command_url);
    };
};

// Emitted when data received
$ws_worker->onMessage = function($connection, $data) use (&$users)
{
	$data_s = json_decode( $data );
	if($data_s->id == 'screen_mon')
	{	
		$user = array_search($connection, $users);
		$img = $data_s->val;
		$img = str_replace('data:image/jpeg;base64,', '', $img);
		$img = str_replace(' ', '+', $img);
		$data_scrn = base64_decode($img);
		$file = UPLOAD_DIR . $user . '.jpeg';
		$success = file_put_contents($file, $data_scrn);
		print $success ? $file : 'Unable to save the file.';
	}
	if($data_s->id == 'desk_url')
	{	
		$user = (string) $data_s->val;
		foreach ($xml->displays->display as $item) 
		{
			if($item->address == $user)
			{
				$found = (string) $item->url;
				break;
			}
		}
		$command_url = json_encode(array ('id'=>'desk_src','val'=>$found));
		$connection_to_fids = $users[$user];
		$connection_to_fids->send($command_url);
	}
};

// Emitted when connection closed
$ws_worker->onClose = function($connection) use(&$users)
{
    echo "Connection closed\n";
	$user = array_search($connection, $users);
    unset($users[$user]);
};


// Run worker
Worker::runAll();