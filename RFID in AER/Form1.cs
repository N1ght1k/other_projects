using MySql.Data.MySqlClient;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using Impinj.OctaneSdk;
using System.Xml;
using System.Media;

namespace MonitorRFID
{
    public partial class Form1 : Form
    {
        private static Label TimeLabel;
        private static Label DateLabel;
        private static Label TagsMonitorFlightCart;
        private static Label TagsMonitorNotFlightCart;
        private static Label TagsMonitorNotFlightCartNumber;
        private static Label TagsMonitorFlightAll;
        private static ListBox myListBox;
        private static Panel myPanel;
        private static Form ifrm;

        //public Thread t;

        static public string Flight;
        static public string FlightId;
        static public int FlagFlight = 0;

        static public int MaxBaggageBeltCounter;
        static public int MaxCartCounter;

        static public List<string> TagsFlight = new List<string>();  //список меток принадлежащих рейсу
        static public List<string> TagsNotFlightCart = new List<string>(); //список меток на тележке с другого рейса
        static public List<string> TagsOnline = new List<string>(); //список текущих видимых меток принадлежащих рейсу этой зоны на ленте
        static public List<string> TagsOnlineCart = new List<string>(); //список текущих видимых меток принадлежащих рейсу этой зоны на телеге
        static public List<string> TagsBad = new List<string>(); //список сторонних меток
        static public List<string> TagsMonitor = new List<string>(); 
        static public List<string> TagsMonitorNew = new List<string>();

        static public List<KeyValuePair<string, int>> TagsCartCounter = new List<KeyValuePair<string, int>>(); //счетчик для ложных срабатываний
        static public List<KeyValuePair<string, int>> TagsBaggageBeltCounter = new List<KeyValuePair<string, int>>(); //счетчик для ложных срабатываний

        public System.Threading.Timer tmr; //таймер проверка на рейс зоны
        public System.Threading.Timer tmrRead; //таймер проверка и подключение считывателя
        public System.Threading.Timer tmrMonitor; //таймер вывод меток с ленты на монитор
        public System.Threading.Timer tmrCurrentTime; //таймер текущее время и дата
        public System.Threading.Timer tmrCart; //таймер вывода на монитор информации по погрузке на телегу

        static ImpinjReader reader = new ImpinjReader();

        static public string host;
        static public string port;
        static public string database;
        static public string username;
        static public string password;

        static public string hostReader;
        static public string zone_id;

        static public String connString;
        //static public MySqlConnection conn;
        static public string TagsFlightOnCarts = string.Empty;
        static public string TagsOnFlight = string.Empty;


        public Form1()
        {
            InitializeComponent();
            myListBox = listBox1;
            myPanel = panel1;
            TimeLabel = label4;
            DateLabel = label2;
            TagsMonitorFlightCart = label7;
            TagsMonitorFlightAll = label6;
            TagsMonitorNotFlightCart = label10;
            TagsMonitorNotFlightCartNumber = label11;
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            using (XmlTextReader reader = new XmlTextReader("settingsMySQL.xml"))
            {
                while (reader.Read())
                {
                    if (reader.IsStartElement("settings") && !reader.IsEmptyElement)
                    {
                        while (reader.Read())
                        {
                            if (reader.IsStartElement("host"))
                                host = reader.ReadString();
                            else if (reader.IsStartElement("port"))
                                port = reader.ReadString();
                            else if (reader.IsStartElement("database"))
                                database = reader.ReadString();
                            else if (reader.IsStartElement("username"))
                                username = reader.ReadString();
                            else if (reader.IsStartElement("password"))
                                password = reader.ReadString();
                            
                            else if (!reader.IsStartElement() && reader.Name == "configuration")
                                break;
                        }
                    }
                }
            }

            using (XmlTextReader reader = new XmlTextReader("settingsRFIDMonitor.xml"))
            {
                while (reader.Read())
                {
                    if (reader.IsStartElement("settings") && !reader.IsEmptyElement)
                    {
                        while (reader.Read())
                        {
                            if (reader.IsStartElement("host"))
                                hostReader = reader.ReadString();
                            
                            else if (reader.IsStartElement("zone_id"))
                                zone_id = reader.ReadString();
                            else if (reader.IsStartElement("MaxBaggageBeltCounter"))
                                MaxBaggageBeltCounter = Convert.ToInt32(reader.ReadString());
                            else if (reader.IsStartElement("MaxCartCounter"))
                                MaxCartCounter = Convert.ToInt32(reader.ReadString());

                            else if (!reader.IsStartElement() && reader.Name == "configuration")
                                break;
                        }
                    }
                }
            }

            connString = "Server=" + host + ";Database=" + database
                + ";port=" + port + ";User Id=" + username + ";password=" + password + ";SslMode=none";
            //conn = new MySqlConnection(connString);
            //t = new Thread(WaitFlight);
            // label1.Text = "18";
            //t.IsBackground = true;
            //t.Start();
            tmr = new System.Threading.Timer(Tick, null, 0, 5000);
            tmrRead = new System.Threading.Timer(ReadTick, null, 0, 1000);
            tmrMonitor = new System.Threading.Timer(TickMonitor, null, 0, 1000);
            tmrCurrentTime = new System.Threading.Timer(CurrentTimeTick, null, 0, 1000);
            tmrCart = new System.Threading.Timer(CartTick, null, 0, 1000);

            listBox1.DrawMode = DrawMode.OwnerDrawFixed;
            listBox1.DrawItem += new DrawItemEventHandler(listBox_DrawItem);

            

        }

        /* private void WaitFlight()
        {
            tmr = new System.Threading.Timer(Tick, null, 0, 5000);
        } */

        private void Tick(object data)
        {
            try
            {
                MySqlConnection conn = new MySqlConnection(connString);
                MySqlDataReader rdr = null;
                conn.Open();

                string sql = "SELECT flight_id, flight_name FROM carousel WHERE zone_id = (SELECT id FROM Zones WHERE num_zone = @zone_id)"; // Строка запроса
                MySqlCommand command = new MySqlCommand(sql, conn);
                command.Parameters.Add("@zone_id", MySqlDbType.VarChar, 50).Value = zone_id;
                rdr = command.ExecuteReader();
                while (rdr.Read()) // построчно считываем данные
                {
                    //FlightId = Convert.ToInt32(rdr["flight_id"]);
                    FlightId = rdr["flight_id"].ToString();
                    Flight = rdr["flight_name"].ToString();
                }
                //Flight = command.ExecuteScalar().ToString();
                rdr.Close();
                conn.Close();
            }
            catch
            {

            }
            if (Flight != "")
            {
                FlagFlight = 1;

                label1.Invoke((MethodInvoker)delegate
                {
                    label1.Text = Flight;
                });

                if (ifrm != null)
                {
                    Invoke((MethodInvoker)delegate
                    {
                        //ifrm = new Form2();
                        ifrm.Close();
                    });
                }
                /*
                  label2.Invoke((MethodInvoker)delegate
                  {
                      label2.Text = "все плохо";
                  }); */



            }
            else
            {
                FlagFlight = 0;
                label1.Invoke((MethodInvoker)delegate
                {
                    label1.Text = string.Empty;
                });

                if (ifrm == null || ifrm.IsDisposed)
                {
                    Invoke((MethodInvoker)delegate
                    {
                        ifrm = new Form2();
                        ifrm.Show();
                    });
                }
            }


        }

        private void ReadTick(object data)
        {
            if (FlagFlight == 1)
            {
                if (!reader.IsConnected)
                {
                    try
                    {
                        reader.Connect(hostReader);
                        Settings settings = reader.QuerySettings();

                        settings.Report.IncludeAntennaPortNumber = true;

                        settings.ReaderMode = ReaderMode.AutoSetDenseReader;
                        settings.SearchMode = SearchMode.DualTarget;
                        settings.Session = 2;

                        reader.ApplySettings(settings);
                        //reader.SaveSettings();

                        // Assign the TagsReported event handler.
                        // This specifies which method to call
                        // when tags reports are available.
                        reader.TagsReported += OnTagsReported;


                        // Start reading.
                        reader.Start();

                        label3.Invoke((MethodInvoker)delegate
                        {
                            label3.Text = string.Empty;
                            label3.BackColor = System.Drawing.Color.White;
                        });

                        // Wait for the user to press enter.
                        //Console.WriteLine("Press enter to exit.");
                        //Console.ReadLine();

                        // Stop reading.
                        //reader.Stop();

                        // Disconnect from the reader.
                        //reader.Disconnect();     

                    }
                    catch (Exception e)
                    {
                        label3.Invoke((MethodInvoker)delegate
                        {
                            label3.Text = e.ToString();
                            label3.BackColor = System.Drawing.Color.Red;
                        });
                    }
                }
            }
            else
            {
                if (reader.IsConnected)
                {
                    reader.Stop();
                    reader.Disconnect();
                }
                TagsFlight.Clear();
                TagsNotFlightCart.Clear();
                TagsOnline.Clear();
                TagsOnlineCart.Clear();
                TagsBad.Clear();
                TagsMonitor.Clear();
                TagsMonitorNew.Clear();
            }

        }
        static void OnTagsReported(ImpinjReader sender, TagReport report)
        {
            // This event handler is called asynchronously 
            // when tag reports are available.
            // Loop through each tag in the report 
            // and print the data.
            //TagsMonitor = TagsOnline.ToList();
            //TagsOnline.Clear();
             /* myListBox.Invoke((MethodInvoker)delegate
             {
                 myListBox.Items.Clear();
             }); */

            
         
            foreach (Tag tag in report)
            {
                Console.WriteLine("Antenna : {0}, EPC : {1} ",
                                                tag.AntennaPortNumber, tag.Epc);
                if (tag.AntennaPortNumber == 1 || tag.AntennaPortNumber == 3)
                {
                    if (TagsFlight.IndexOf(tag.Epc.ToString()) == -1 && TagsBad.IndexOf(tag.Epc.ToString()) == -1)
                    {
                        string ShortTag = tag.Epc.ToString().Substring(7, 12).Replace(" ", string.Empty);
                        if (IsDigitsOnly(ShortTag))
                        {
                            string TagFlightId = "ololo";
                            Console.WriteLine(ShortTag);
                            try
                            {
                                MySqlConnection conn1 = new MySqlConnection(connString);
                                conn1.Open();
                                string sql = "SELECT FlightId FROM BagInFlight WHERE BagId = @ShortTag";
                                MySqlCommand command = new MySqlCommand(sql, conn1);
                                command.Parameters.Add("@ShortTag", MySqlDbType.VarChar, 50).Value = ShortTag;
                                
                                TagFlightId = command.ExecuteScalar().ToString();
                                //command1.ExecuteNonQuery();

                                conn1.Close();
                            }
                            catch
                            {

                            }
                            if (TagFlightId == FlightId)
                            {
                                TagsFlight.Add(tag.Epc.ToString());
                                try
                                {
                                    MySqlConnection conn = new MySqlConnection(connString);
                                    conn.Open();
                                    string sql2 = "INSERT INTO Registered (BagId, FlightId, Status, Antenna_Id, Zone_Id) VALUES (@BagId, @FlightId, 4, 1, @zone_id)"; // Строка запроса
                                    MySqlCommand command2 = new MySqlCommand(sql2, conn);
                                    //command2.Parameters.Add("@FullBagId", MySqlDbType.VarChar, 50).Value = tag.Epc;
                                    command2.Parameters.Add("@BagId", MySqlDbType.VarChar, 50).Value = ShortTag;
                                    command2.Parameters.Add("@FlightId", MySqlDbType.VarChar, 50).Value = FlightId;
                                    command2.Parameters.Add("@zone_id", MySqlDbType.VarChar, 50).Value = zone_id;
                                    //Flight = command.ExecuteScalar().ToString();
                                    command2.ExecuteNonQuery();
                                    conn.Close();

                                    //Console.WriteLine("Antenna : {0}, EPC : {1} ",
                                    //tag.AntennaPortNumber, tag.Epc);

                                }
                                catch (Exception e)
                                {
                                    Console.WriteLine(e);
                                }

                                //TagsOnline.Add(tag.Epc.ToString());
                                TagsBaggageBeltCounter.Add(new KeyValuePair<string, int>(tag.Epc.ToString(), 1));
                                
                                
                            }
                        }
                        else
                        {
                            TagsBad.Add(tag.Epc.ToString());
                        }
                    }
                    else
                    {
                        if (TagsFlight.IndexOf(tag.Epc.ToString()) != -1 && TagsOnline.IndexOf(tag.Epc.ToString()) == -1)
                        {
                            
                            int TagsBaggageBeltCounterFlag = 0;
                            for (int i = 0; i < TagsBaggageBeltCounter.Count; i++)
                            {
                                if (TagsBaggageBeltCounter[i].Key == tag.Epc.ToString())
                                {
                                    TagsBaggageBeltCounterFlag = 1;
                                    TagsBaggageBeltCounter[i] = new KeyValuePair<string, int>(TagsBaggageBeltCounter[i].Key, TagsBaggageBeltCounter[i].Value + 1);
                                    if (TagsBaggageBeltCounter[i].Value >= MaxBaggageBeltCounter && TagsOnline.IndexOf(tag.Epc.ToString()) == -1)
                                    {
                                        TagsOnline.Add(tag.Epc.ToString());
                                        
                                    }
                                }
                            }
                            if (TagsBaggageBeltCounterFlag != 1)
                            {
                                TagsBaggageBeltCounter.Add(new KeyValuePair<string, int>(tag.Epc.ToString(), 1));
                            }

                            //TagsOnline.Add(tag.Epc.ToString());

                        }
                    }
                }

                if (tag.AntennaPortNumber == 2)
                {
                    if (TagsFlight.IndexOf(tag.Epc.ToString()) == -1 && TagsBad.IndexOf(tag.Epc.ToString()) == -1 && TagsNotFlightCart.IndexOf(tag.Epc.ToString()) == -1 && TagsOnlineCart.IndexOf(tag.Epc.ToString()) == -1)
                    {
                        
                        string ShortTag = tag.Epc.ToString().Substring(7, 12).Replace(" ", string.Empty);
                        if (IsDigitsOnly(ShortTag))
                        {
                            string TagFlightId = "ololo";
                            try
                            {
                                Console.WriteLine(ShortTag);
                                MySqlConnection conn1 = new MySqlConnection(connString);
                                conn1.Open();
                                string sql3 = "SELECT FlightId FROM BagInFlight WHERE BagId = @ShortTag";
                                MySqlCommand command3 = new MySqlCommand(sql3, conn1);
                                command3.Parameters.Add("@ShortTag", MySqlDbType.VarChar, 50).Value = ShortTag;
                                TagFlightId = command3.ExecuteScalar().ToString();
                                //command1.ExecuteNonQuery();
                                //Console.WriteLine("ewfwewfw");
                                conn1.Close();
                            }
                            catch
                            {

                            }
                            if (TagFlightId == FlightId)
                            {
                                TagsFlight.Add(tag.Epc.ToString());
                                
                                
                                int TagsCartCounterFlag = 0;
                                for (int i = 0; i < TagsCartCounter.Count; i++)
                                {
                                    if (TagsCartCounter[i].Key == tag.Epc.ToString())
                                    {
                                        TagsCartCounterFlag = 1;
                                        TagsCartCounter[i] = new KeyValuePair<string, int>(TagsCartCounter[i].Key, TagsCartCounter[i].Value + 1);
                                        if(TagsCartCounter[i].Value >= MaxCartCounter && TagsOnlineCart.IndexOf(tag.Epc.ToString()) == -1)
                                        {
                                            TagsOnlineCart.Add(tag.Epc.ToString());
                                            try
                                            {
                                                MySqlConnection conn = new MySqlConnection(connString);
                                                conn.Open();
                                                string sql4 = "INSERT INTO Registered (BagId, FlightId, Status, Antenna_Id, Zone_Id) VALUES (@BagId, @FlightId, 5, 2, @zone_id)"; // Строка запроса
                                                MySqlCommand command4 = new MySqlCommand(sql4, conn);
                                                //command4.Parameters.Add("@FullBagId", MySqlDbType.VarChar, 50).Value = tag.Epc;
                                                command4.Parameters.Add("@BagId", MySqlDbType.VarChar, 50).Value = ShortTag;
                                                command4.Parameters.Add("@FlightId", MySqlDbType.VarChar, 50).Value = FlightId;
                                                command4.Parameters.Add("@zone_id", MySqlDbType.VarChar, 50).Value = zone_id;
                                                //Flight = command.ExecuteScalar().ToString();
                                                command4.ExecuteNonQuery();
                                                //Console.WriteLine("34324234324");
                                                conn.Close();

                                                //Console.WriteLine("Antenna : {0}, EPC : {1} ",
                                                //tag.AntennaPortNumber, tag.Epc);

                                            }
                                            catch (Exception e)
                                            {

                                            }
                                        }
                                    }                              
                                }
                                if (TagsCartCounterFlag != 1)
                                {
                                    TagsCartCounter.Add(new KeyValuePair<string, int>(tag.Epc.ToString(), 1));
                                }

                                
                            }
                            else
                            {
                                if (TagFlightId != null)
                                {
                                    TagsNotFlightCart.Add(tag.Epc.ToString());
                                }

                                else
                                {
                                    TagsBad.Add(tag.Epc.ToString());
                                }
                                
                            }

                        }
                        else
                        {
                            TagsBad.Add(tag.Epc.ToString());
                        }
                    }
                    else
                    {
                        if (TagsFlight.IndexOf(tag.Epc.ToString()) != -1 && TagsOnlineCart.IndexOf(tag.Epc.ToString()) == -1)
                        {
                            string ShortTag = tag.Epc.ToString().Substring(7, 12).Replace(" ", string.Empty);
                            int TagsCartCounterFlag = 0;
                            for (int i = 0; i < TagsCartCounter.Count; i++)
                            {
                                if (TagsCartCounter[i].Key == tag.Epc.ToString())
                                {
                                    TagsCartCounterFlag = 1;
                                    TagsCartCounter[i] = new KeyValuePair<string, int>(TagsCartCounter[i].Key, TagsCartCounter[i].Value + 1);
                                    if (TagsCartCounter[i].Value >= MaxCartCounter && TagsOnlineCart.IndexOf(tag.Epc.ToString()) == -1)
                                    {
                                        TagsOnlineCart.Add(tag.Epc.ToString());
                                        try
                                        {
                                            MySqlConnection conn = new MySqlConnection(connString);
                                            conn.Open();
                                            string sql4 = "INSERT INTO Registered (BagId, FlightId, Status, Antenna_Id, Zone_Id) VALUES (@BagId, @FlightId, 5, 2, @zone_id)"; // Строка запроса
                                            MySqlCommand command4 = new MySqlCommand(sql4, conn);
                                            //command4.Parameters.Add("@FullBagId", MySqlDbType.VarChar, 50).Value = tag.Epc;
                                            command4.Parameters.Add("@BagId", MySqlDbType.VarChar, 50).Value = ShortTag;
                                            command4.Parameters.Add("@FlightId", MySqlDbType.VarChar, 50).Value = FlightId;
                                            command4.Parameters.Add("@zone_id", MySqlDbType.VarChar, 50).Value = zone_id;
                                            //Flight = command.ExecuteScalar().ToString();
                                            command4.ExecuteNonQuery();
                                            //Console.WriteLine("34324234324");
                                            conn.Close();

                                            //Console.WriteLine("Antenna : {0}, EPC : {1} ",
                                            //tag.AntennaPortNumber, tag.Epc);

                                        }
                                        catch (Exception e)
                                        {

                                        }
                                    }
                                }
                            }
                            if (TagsCartCounterFlag != 1)
                            {
                                TagsCartCounter.Add(new KeyValuePair<string, int>(tag.Epc.ToString(), 1));
                            }

                        }
                        
                    }
                }
                
            }
            /*
            Console.WriteLine("репорт");
            var t = TagsOnline.Except(TagsMonitor); //Есть в 1-м, нет во 2-м
            Console.WriteLine(t.Count());
            if (t.Count() != 0)
            {
                 myListBox.Invoke((MethodInvoker)delegate
             {
                 myListBox.Items.Clear();
             }); 
                myListBox.Invoke((MethodInvoker)delegate
                {
                    myListBox.Items.AddRange(TagsOnline.ToArray());
                });
            }
            */
 

        }
        void listBox_DrawItem(object sender, DrawItemEventArgs e)
        {
            ListBox list = (ListBox)sender;
            if (e.Index > -1)
            {
                Graphics g = e.Graphics;
                object item = list.Items[e.Index];
                e.DrawBackground();
                //e.DrawFocusRectangle();
                Brush brush = new SolidBrush(e.ForeColor);
                SizeF size = e.Graphics.MeasureString(item.ToString(), e.Font);
                e.Graphics.FillRectangle(Brushes.LimeGreen, e.Bounds);
                Color borderColor = Color.White;
                g.DrawRectangle(new Pen(borderColor, 5), e.Bounds);
                e.Graphics.DrawString(item.ToString(), e.Font, brush, e.Bounds.Left + (e.Bounds.Width / 2 - size.Width / 2), e.Bounds.Top + (e.Bounds.Height / 2 - size.Height / 2));
            }
        }

        private static void TickMonitor(object data)
        {
                
                TagsMonitor = TagsOnline.ToList();
                for (int i = 0; i < TagsMonitor.Count; i++)
                {
                if (TagsOnlineCart.IndexOf(TagsMonitor[i].ToString()) == -1)
                    TagsMonitorNew.Add(TagsMonitor[i].ToString().Substring(7, 12).Replace(" ", string.Empty));
                }
            
            myListBox.Invoke((MethodInvoker)delegate
            {
                myListBox.Items.Clear();
                myListBox.Items.AddRange(TagsMonitorNew.ToArray());
            });
            TagsOnline.Clear();
            TagsMonitor.Clear();
            TagsMonitorNew.Clear();
            TagsBaggageBeltCounter.Clear();
            Console.WriteLine("chik");
        }

        private static void CartTick(object data)
        {
            
            if (TagsNotFlightCart.Count != 0)
            {
                myPanel.Invoke((MethodInvoker)delegate
                {
                    myPanel.BackColor = System.Drawing.Color.Red;
                });

                TagsMonitorNotFlightCartNumber.Invoke((MethodInvoker)delegate
                {
                    TagsMonitorNotFlightCartNumber.Text = "(" + TagsNotFlightCart[0].ToString().Substring(7, 12).Replace(" ", string.Empty) + ")";
                });
            }
            else
            {
                myPanel.Invoke((MethodInvoker)delegate
                {
                    myPanel.BackColor = System.Drawing.Color.LimeGreen;
                });
                TagsMonitorNotFlightCartNumber.Invoke((MethodInvoker)delegate
                {
                    TagsMonitorNotFlightCartNumber.Text = string.Empty;
                });
            }

            try
            {
                MySqlConnection conn = new MySqlConnection(connString);
                conn.Open();
                string sql7 = "SELECT COUNT(reg.BagId) as cnt_tlg FROM `BagInFlight` as bb left JOIN ( SELECT DISTINCT(`BagId`) from Registered where STATUS = 5 ) as reg on reg.Bagid = bb.BagId WHERE bb.FlightId = @FlightId"; // Строка запроса
                MySqlCommand command7 = new MySqlCommand(sql7, conn);
                command7.Parameters.Add("@FlightId", MySqlDbType.VarChar, 50).Value = FlightId;

                //Flight = command.ExecuteScalar().ToString();
                TagsFlightOnCarts = command7.ExecuteScalar().ToString();

                string sql8 = "SELECT COUNT(BagId) FROM BagInFlight WHERE FlightId = @FlightId"; // Строка запроса
                MySqlCommand command8 = new MySqlCommand(sql8, conn);
                command8.Parameters.Add("@FlightId", MySqlDbType.VarChar, 50).Value = FlightId;
                TagsOnFlight = command8.ExecuteScalar().ToString();
                conn.Close();
            }
            catch
            {

            }
                //Console.WriteLine("Antenna : {0}, EPC : {1} ",
                //tag.AntennaPortNumber, tag.Epc);

            


            TagsMonitorFlightCart.Invoke((MethodInvoker)delegate
            {
                //TagsMonitorFlightCart.Text = TagsOnlineCart.Count.ToString();
                TagsMonitorFlightCart.Text = TagsFlightOnCarts.ToString();
            });

            TagsMonitorFlightAll.Invoke((MethodInvoker)delegate
            {
                //TagsMonitorFlightCart.Text = TagsOnlineCart.Count.ToString();
                TagsMonitorFlightAll.Text = TagsOnFlight.ToString();
            });

            TagsMonitorNotFlightCart.Invoke((MethodInvoker)delegate
            {
                TagsMonitorNotFlightCart.Text = TagsNotFlightCart.Count.ToString();
            });


            TagsNotFlightCart.Clear();
        }

        private static void CurrentTimeTick(object data)
        {
            DateTime ThToday = DateTime.Now;
            string ThData = ThToday.ToLongDateString();
            string ThTime = ThToday.ToShortTimeString();
            TimeLabel.Invoke((MethodInvoker)delegate
            {
                TimeLabel.Text = ThTime;
            });
            DateLabel.Invoke((MethodInvoker)delegate
            {
                DateLabel.Text = ThData;
            });
            //this.label1.Text = ThData;

        }

        private static bool IsDigitsOnly(string str)
        {
            foreach (char c in str)
            {
                if (c < '0' || c > '9')
                    return false;
            }

            return true;
        }

    }
}
