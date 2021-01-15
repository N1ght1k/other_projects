using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Xml;
using MySql.Data.MySqlClient;

namespace AnalysisDB
{
    public partial class Form1 : Form
    {
        public System.Threading.Timer tmr;

        static public string host;
        static public string port;
        static public string database;
        static public string username;
        static public string password;

        static public string BagId;
        static public string NumZone;
        static public string DTime;

        static public String connString;

        public Form1()
        {
            InitializeComponent();
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

            connString = "Server=" + host + ";Database=" + database
                + ";port=" + port + ";User Id=" + username + ";password=" + password + ";SslMode=none";

            tmr = new System.Threading.Timer(Tick, null, 0, 10000);

        }

        private void Tick(object data)
        {
            try
            {
                MySqlConnection conn = new MySqlConnection(connString);
                MySqlDataReader rdr = null;
                conn.Open();

                string sql = "SELECT BagId, NumZone, DTime FROM Sorting WHERE (CURRENT_TIMESTAMP - DTime) >= 10"; // Строка запроса
                MySqlCommand command = new MySqlCommand(sql, conn);
                rdr = command.ExecuteReader();
                while (rdr.Read()) // построчно считываем данные
                {
                    //FlightId = Convert.ToInt32(rdr["flight_id"]);
                    BagId = rdr["BagId"].ToString();
                    NumZone = rdr["NumZone"].ToString();
                    DTime = rdr["DTime"].ToString();
                    MySqlConnection conn1 = new MySqlConnection(connString);
                    conn1.Open();
                    string sql1 = "INSERT INTO Registered (BagId, Zone_Id, Status) VALUES (@BagId, @NumZone, 5)"; // Строка запроса
                    MySqlCommand command1 = new MySqlCommand(sql1, conn1);
                    command1.Parameters.Add("@BagId", MySqlDbType.VarChar, 50).Value = BagId;
                    command1.Parameters.Add("@NumZone", MySqlDbType.VarChar, 50).Value = NumZone;
                    command1.ExecuteNonQuery();
                    string sql2 = "DELETE FROM Sorting WHERE BagId = @BagId"; // Строка запроса
                    MySqlCommand command2 = new MySqlCommand(sql2, conn1);
                    command2.Parameters.Add("@BagId", MySqlDbType.VarChar, 50).Value = BagId;
                    command2.ExecuteNonQuery();
                    conn1.Close();
                }
                //Flight = command.ExecuteScalar().ToString();
                rdr.Close();
                conn.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        }
    }
}
