using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace MonitorRFID
{
    public partial class Form2 : Form
    {
        public System.Threading.Timer tmrNumber;
        Random rnd = new Random();

        public Form2()
        {
            InitializeComponent();
        }

        private void Form2_Load(object sender, EventArgs e)
        {
            tmrNumber = new System.Threading.Timer(TickNumber, null, 0, 3000);
            label1.Text = Form1.zone_id;
        }

        private void TickNumber(object data)
        {

            int randomX = rnd.Next(0, Size.Width - label1.Size.Width);
            int randomY = rnd.Next(0, Size.Height - label1.Size.Height);
            label1.Invoke((MethodInvoker)delegate
            {
                label1.Location = new Point(randomX, randomY);
            });
        }
    }
}
