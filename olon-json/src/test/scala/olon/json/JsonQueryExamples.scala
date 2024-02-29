package olon
package json

import org.specs2.mutable.Specification

/** System under specification for JSON Query Examples.
  */
object JsonQueryExamples extends Specification {
  "JSON Query Examples".title

  "List of IPs" in {
    // SCALA3 Adding `case` for correct syntax desugaring
    val ips = for {
      case JField("ip", JString(ip)) <- (json \\ "ip").obj
    } yield {
      ip
    }

    ips mustEqual List(
      "192.168.1.125",
      "192.168.1.126",
      "192.168.1.127",
      "192.168.2.125",
      "192.168.2.126"
    )
  }

  "List of IPs converted to XML" in {
    val ipsList = (json \\ "ip").obj

    // SCALA3 Adding `case` for correct syntax desugaring
    val ips = <ips>{
      for {
        field <- ipsList
        case JString(ip) <- field.value
      } yield <ip>{ip}</ip>
    }</ips>

    ips mustEqual <ips><ip>192.168.1.125</ip><ip>192.168.1.126</ip><ip>192.168.1.127</ip><ip>192.168.2.125</ip><ip>192.168.2.126</ip></ips>
  }

  "List of IPs in cluster2" in {
    // SCALA3 Adding `case` for correct syntax desugaring
    val ips = for {
      case JObject(x) <- json \ "data_center"
      if (x contains JField("name", JString("cluster2")))
      case JField("ip", JString(ip)) <- (JObject(x) \\ "ip").obj
    } yield {
      ip
    }

    ips mustEqual List("192.168.2.125", "192.168.2.126")
  }

  "Total cpus in data center" in {
    // SCALA3 Adding `case` for correct syntax desugaring
    val computerCpuCount = for {
      case JField("cpus", JInt(x)) <- (json \\ "cpus").obj
    } yield {
      x
    }

    computerCpuCount reduceLeft (_ + _) mustEqual 40
  }

  "Servers sorted by uptime" in {
    case class Server(ip: String, uptime: Long)

    // SCALA3 Adding `case` for correct syntax desugaring
    val servers = for {
      case JField("servers", JArray(servers)) <- (json \\ "servers").obj
      case JObject(server) <- servers
      case JField("ip", JString(ip)) <- server
      case JField("uptime", JInt(uptime)) <- server
    } yield {
      Server(ip, uptime.longValue)
    }

    servers sortWith (_.uptime > _.uptime) mustEqual List(
      Server("192.168.1.127", 901214),
      Server("192.168.2.125", 453423),
      Server("192.168.2.126", 214312),
      Server("192.168.1.126", 189822),
      Server("192.168.1.125", 150123)
    )
  }

  "Clusters administered by liza" in {
    // SCALA3 Adding `case` for correct syntax desugaring
    val clusters = for {
      case JObject(cluster) <- json
      case JField("admins", JArray(admins)) <- cluster
      if admins contains JString("liza")
      case JField("name", JString(name)) <- cluster
    } yield name

    clusters.mustEqual(List("cluster2"))
  }

  val json = parse("""
    { "data_center": [
      {
        "name": "cluster1",
        "servers": [
          {"ip": "192.168.1.125", "uptime": 150123, "specs": {"cpus":  8, "ram": 2048}},
          {"ip": "192.168.1.126", "uptime": 189822, "specs": {"cpus": 16, "ram": 4096}},
          {"ip": "192.168.1.127", "uptime": 901214, "specs": {"cpus":  8, "ram": 4096}}
        ],
        "links": [
          {"href": "http://www.example.com/admin", "name": "admin"},
          {"href": "http://www,example.com/home", "name": "home"}
        ],
        "admins": ["jim12", "joe", "maddog"]
      },
      {
        "name": "cluster2",
        "servers": [
          {"ip": "192.168.2.125", "uptime": 453423, "specs": {"cpus":  4, "ram": 2048}},
          {"ip": "192.168.2.126", "uptime": 214312, "specs": {"cpus":  4, "ram": 2048}},
        ],
        "links": [
          {"href": "http://www.example2.com/admin", "name": "admin"},
          {"href": "http://www,example2.com/home", "name": "home"}
        ],
        "admins": ["joe", "liza"]
      }
   ]}
  """)
}
